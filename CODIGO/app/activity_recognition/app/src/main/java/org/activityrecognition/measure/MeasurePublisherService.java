package org.activityrecognition.measure;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.client.model.ModelClientFactory;
import org.activityrecognition.client.model.MeasureRequest;
import org.activityrecognition.user.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeasurePublisherService extends IntentService implements PacketListener {

    private final String TAG = "ACTREC_MEASURE";
    private ModelClient modelClient;
    private String userId;
    private SessionManager session;

    public MeasurePublisherService() {
        super("MeasurePublisherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        SensorPacketCollector sensorPacketCollector = new SensorPacketCollector(sensorManager);
        sensorPacketCollector.start();

        modelClient = ModelClientFactory.getClient();

        sensorPacketCollector.registerListener(this);

        userId = intent.getStringExtra("USER_ID");
        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        // run for certain indicated time
        int collectionTime = intent.getIntExtra("COLLECTION_TIME_SEC", 60);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            sensorPacketCollector.stop();
            stopSelf();
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
