package org.activityrecognition;

import android.content.DialogInterface;
import android.graphics.ColorSpace;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.client.model.MeasureRequest;
import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.client.model.ModelClientFactory;
import org.activityrecognition.client.model.ModelState;
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
    private SensorCollectorForTrain sensorPacketCollector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        int collectionTime = getIntent().getIntExtra("COLLECTION_TIME_SEC", 60);
        userId = getIntent().getStringExtra("USER_ID");

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorPacketCollector = new SensorCollectorForTrain(sensorManager);

        // run for certain indicated time
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            sensorPacketCollector.stop();
            sensorPacketCollector.unregisterListener();
            if (userId.equals("1")) {
                session.setModelState(ModelState.COLLECTED_1);
            } else {
                session.setModelState(ModelState.COLLECTED_2);
            }
            AlertDialog alertDialog = new AlertDialog.Builder(CollectActivity.this).create();
            alertDialog.setMessage("Recolección de datos finalizada");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ACEPTAR",
                    (dialog, which) -> {
                        dialog.dismiss();
                        CollectActivity.this.finish();
                    });
            alertDialog.show();
        }, collectionTime * 1000);
    }

    ModelClient getModelClient() {
        if (modelClient == null) {
            modelClient = ModelClientFactory.getClient();
        }
        return modelClient;
    }

    @Override
    public void onPackageComplete(List<String> packet) {
        // launch a thread with the http call to the external service
        MeasureRequest request = new MeasureRequest(packet);
        Call<Void> call = getModelClient().pushMeasures(session.getModelName(), userId, request);
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


    @Override
    protected void onPause() {
        super.onPause();
        sensorPacketCollector.stop();
        sensorPacketCollector.unregisterListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorPacketCollector.start();
        sensorPacketCollector.registerListener(this);
    }
}
