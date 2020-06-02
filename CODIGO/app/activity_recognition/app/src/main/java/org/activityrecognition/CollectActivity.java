package org.activityrecognition;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.client.model.MeasureRequest;
import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.client.model.ModelClientFactory;
import org.activityrecognition.measure.PacketListenerTrain;
import org.activityrecognition.measure.SensorCollectorForTrain;
import org.activityrecognition.user.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectActivity extends AppCompatActivity implements PacketListenerTrain {
    private final String TAG = "ACTREC_TRAIN";

    private SessionManager session;
    private ModelClient modelClient;
    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        int collectionTime = getIntent().getIntExtra("COLLECTION_TIME_SEC", 60);
        userId = getIntent().getStringExtra("USER_ID");

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        SensorCollectorForTrain sensorPacketCollector = new SensorCollectorForTrain(sensorManager);
        sensorPacketCollector.registerListener(this);
        sensorPacketCollector.start();

        modelClient = ModelClientFactory.getClient();

        // run for certain indicated time
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            sensorPacketCollector.stop();
            finish();
        }, collectionTime * 1000);
    }

    @Override
    public void onPackageComplete(List<String> packet) {
        // launch a thread with the http call to the external service
        MeasureRequest request = new MeasureRequest(packet);
        Call<Void> call = modelClient.pushMeasures(session.getModelName(), userId, request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "packet pushed successfully!");
                } else {
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Unable to submit post to API. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }
}
