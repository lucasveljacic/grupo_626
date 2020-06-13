package org.activityrecognition.ui.predict;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import org.activityrecognition.R;
import org.activityrecognition.external.client.model.ModelClient;
import org.activityrecognition.external.client.model.ModelClientFactory;
import org.activityrecognition.external.client.model.PredictionInputDTO;
import org.activityrecognition.external.client.model.PredictionOutputDTO;
import org.activityrecognition.BaseActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictActivity extends BaseActivity implements PacketListenerForPredict {
    private final String TAG = "ACTREC_PREDICTION";
    private ModelClient modelClient;
    private Button closeButton;
    private Button predictionButton;
    private SensorCollectorForPrediction sensorCollectorForPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

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
        if (isOffline()) {
            interruptPrediction();
        }

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
                        txtPrediction = "USUARIO 1 - (" + p + "%)";
                    } else {
                        p = (int) (100*prediction);
                        txtPrediction = "USUARIO 2 - (" + p + "%)";
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

    @Override
    public void onPause() {
        super.onPause();
        sensorCollectorForPrediction.stop();
        sensorCollectorForPrediction.unregisterListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorCollectorForPrediction.start();
        sensorCollectorForPrediction.registerListener(this);
    }

    @Override
    protected void disableControls() {

    }

    @Override
    protected void updateView() {

    }

    private void interruptPrediction() {
        sensorCollectorForPrediction.unregisterListener();
        sensorCollectorForPrediction.stop();
        AlertDialog alertDialog = new AlertDialog.Builder(PredictActivity.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(getString(R.string.offline_error_msg));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ACEPTAR",
                (dialog, which) -> {
                    PredictActivity.this.finish();
                });
        alertDialog.show();
    }
}
