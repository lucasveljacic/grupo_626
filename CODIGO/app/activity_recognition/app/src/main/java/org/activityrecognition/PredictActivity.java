package org.activityrecognition;

import android.gesture.Prediction;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.FontResourcesParserCompat;

import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.client.model.ModelClientFactory;
import org.activityrecognition.client.model.PredictionInputDTO;
import org.activityrecognition.client.model.PredictionOutputDTO;
import org.activityrecognition.measure.PacketListenerPredict;
import org.activityrecognition.measure.SensorCollectorForPrediction;
import org.activityrecognition.user.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictActivity extends AppCompatActivity implements PacketListenerPredict {
    private final String TAG = "ACTREC_PREDICTION";
    private ModelClient modelClient;
    private SessionManager session;
    private Button closeButton;
    private Button predictionButton;
    private SensorCollectorForPrediction sensorCollectorForPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        predictionButton = findViewById(R.id.btn_prediction);

        closeButton = findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> backMenu());

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorCollectorForPrediction = new SensorCollectorForPrediction(sensorManager);
        sensorCollectorForPrediction.start();

        sensorCollectorForPrediction.registerListener(this);
    }

    private ModelClient getClient() {
        if (modelClient == null) {
            modelClient = ModelClientFactory.getClient();
        }
        return modelClient;
    }

    private void backMenu() {
        sensorCollectorForPrediction.stop();
        sensorCollectorForPrediction.unregisterListener();
        finish();
    }

    @Override
    public void onPackageComplete(float[][][] input) {
        PredictionInputDTO request = new PredictionInputDTO();
        request.setInput(input);
        Call<PredictionOutputDTO> call = getClient().predict(session.getModelName(), request);
        call.enqueue(new Callback<PredictionOutputDTO>() {
            @Override
            public void onResponse(Call<PredictionOutputDTO> call, Response<PredictionOutputDTO> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;

                    float prediction = response.body().getPrediction();
                    String txtPrediction;
                    int p;
                    if (prediction < 0.5) {
                        p = (int) (100*(1-prediction));
                        txtPrediction = "USUARIO 1 - " + p + "%";
                    } else {
                        p = (int) (100*prediction);
                        txtPrediction = "USUARIO 2 - " + p + "%";
                    }

                    Log.i(TAG, String.format("Prediction: %f", response.body().getPrediction()));

                    predictionButton.setText(txtPrediction);
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
