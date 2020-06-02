package org.activityrecognition.measure;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.activityrecognition.client.model.MeasureRequest;
import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.client.model.ModelClientFactory;
import org.activityrecognition.client.model.PredictionInputDTO;
import org.activityrecognition.client.model.PredictionOutputDTO;
import org.activityrecognition.user.SessionManager;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionService extends IntentService implements PacketListenerPredict {

    private final String TAG = "ACTREC_PREDICTION";
    private ModelClient modelClient;
    private SessionManager session;

    public PredictionService() {
        super("PredictionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        SensorCollectorForPrediction sensorCollectorForPrediction = new SensorCollectorForPrediction(sensorManager);
        sensorCollectorForPrediction.start();

        modelClient = ModelClientFactory.getClient();

        sensorCollectorForPrediction.registerListener(this);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        // run for certain indicated time
        int collectionTime = intent.getIntExtra("COLLECTION_TIME_SEC", 60);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            sensorCollectorForPrediction.stop();
            stopSelf();
        }, collectionTime * 1000);
    }

    @Override
    public void onPackageComplete(float[][][] input) {
        PredictionInputDTO request = new PredictionInputDTO();
        request.setInput(input);
        Call<PredictionOutputDTO> call = modelClient.predict(session.getModelName(), request);
        call.enqueue(new Callback<PredictionOutputDTO>() {
            @Override
            public void onResponse(Call<PredictionOutputDTO> call, Response<PredictionOutputDTO> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Log.i(TAG, String.format("Prediction: %f", response.body().getPrediction()));

                } else {
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<PredictionOutputDTO> call, Throwable t) {
                Log.e(TAG, "Unable to submit post to API. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }
}
