package org.activityrecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import org.activityrecognition.client.model.EventResponseDTO;
import org.activityrecognition.client.model.ModelDTO;
import org.activityrecognition.client.model.ModelEvent;
import org.activityrecognition.client.model.ModelState;
import org.activityrecognition.event.EventTrackerService;
import org.activityrecognition.event.EventType;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {
    private static final int COLLECTION_MAX_PACKS = 10;
    private final String TAG = "ACTREC_MENU";

    private Button collectUser1Button;
    private Button collectUser2Button;
    private Button trainButton;
    private Button predictButton;
    private Button resetButton;
    private Button logoutButton;
    private EventTrackerService eventTrackerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "performing onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        collectUser1Button = findViewById(R.id.btn_collect_user_1);
        collectUser2Button = findViewById(R.id.btn_collect_user_2);
        resetButton = findViewById(R.id.btn_reset);
        logoutButton = findViewById(R.id.btn_loguot);
        trainButton = findViewById(R.id.btn_train);
        predictButton = findViewById(R.id.btn_predict);

        collectUser1Button.setOnClickListener(v -> collectUser1Metrics());
        collectUser2Button.setOnClickListener(v -> collectUser2Metrics());
        predictButton.setOnClickListener(v -> startPrediction());
        trainButton.setOnClickListener(v -> startTraining());
        resetButton.setOnClickListener(v -> resetModel());
        logoutButton.setOnClickListener(v -> logout());

        loadModelState();
        if (session.getModelState() == null) {
            createModel();
        }

        eventTrackerService = new EventTrackerService(session);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "performing onResume()");
        updateView();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void createModel() {
        disableActions();
        String modelName = session.getModelName();
        // launch a thread with the http call to the external service
        Call<ModelDTO> call = getModelClient().create(modelName);
        call.enqueue(new Callback<ModelDTO>() {
            @Override
            public void onResponse(Call<ModelDTO> call, Response<ModelDTO> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Model created successfully!");
                    session.setModelState(response.body().getState());
                    eventTrackerService.pushEvent(EventType.MODEL_CREATED, String.format("Modelo %s creado exitosamente", modelName));
                    updateView();
                } else {
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<ModelDTO> call, Throwable t) {
                Log.e(TAG, "Unable to submit post to API. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void logout() {
        session.logoutUser();
    }

    private void resetModel() {
        sendModelTransition(ModelEvent.RESET);
    }

    private void startTraining() {
        if (isOffline()) {
            return;
        }
        disableActions();
        TriggerTrainingAsyncTaskRunner runner = new TriggerTrainingAsyncTaskRunner();
        runner.execute();
    }

    private void collectUser1Metrics() {
        if (isOffline()) {
            return;
        }
        disableActions();
        collectUserMetrics("1");
    }
    private void collectUser2Metrics() {
        if (isOffline()) {
            return;
        }
        disableActions();
        collectUserMetrics("2");
    }

    private void collectUserMetrics(String id) {
        disableActions();
        Intent intent = new Intent(getApplicationContext(), CollectActivity.class);
        intent.putExtra("USER_ID", id);
        intent.putExtra("COLLECTION_MAX_PACKS", COLLECTION_MAX_PACKS);
        startActivity(intent);
    }

    private void startPrediction() {
        if (isOffline()) {
            return;
        }
        Intent intent = new Intent(getApplicationContext(), PredictActivity.class);
        startActivity(intent);
    }

    @Override
    protected void disableActions() {
        collectUser1Button.setEnabled(false);
        collectUser2Button.setEnabled(false);
        trainButton.setEnabled(false);
        predictButton.setEnabled(false);
    }

    @Override
    protected void updateView() {
        disableActions();
        ModelState modelState = session.getModelState();

        if (modelState == null) {
            return;
        }

        switch (modelState) {
            case NEW:
            case COLLECTING_1:
                collectUser1Button.setEnabled(true);
                break;
            case COLLECTED_1:
            case COLLECTING_2:
                collectUser2Button.setEnabled(true);
                break;
            case COLLECTED_2:
                trainButton.setEnabled(true);
                break;
            case TRAINING:
            case READY_TO_SERVE:
                startTraining();
                break;
            case SERVING:
                predictButton.setEnabled(true);
                break;
        }
    }

    // inner class to handle training waiting
    private class TriggerTrainingAsyncTaskRunner extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            String modelName = session.getModelName();

            try {
                // launch a thread with the http call to the external service
                if (session.getModelState() == ModelState.COLLECTED_2) {
                    session.setModelState(ModelState.TRAINING);
                    Call<EventResponseDTO> call = getModelClient().pushEvent(modelName, ModelEvent.START_TRAINING.name());
                    Response<EventResponseDTO> response = call.execute();
                    if (!response.isSuccessful()) {
                        Log.e(TAG, response.message());
                        return "FAILURE";
                    } else {
                        Log.i(TAG, String.format("Event %s sent successfully!", ModelEvent.START_TRAINING.name()));
                    }
                }

                // start polling for training completion
                Call<ModelDTO> callGet;
                Response<ModelDTO> responseGet;
                ModelState state = null;

                int totalSleepSeconds = 0;
                do {
                    if (isOffline()) {
                        return "ERROR";
                    }
                    callGet = getModelClient().get(modelName);
                    responseGet = callGet.execute();
                    if (responseGet.isSuccessful()) {
                        state = responseGet.body().getState();
                    }
                    Thread.sleep(5 * 1000);
                    totalSleepSeconds += 5;
                } while (state != ModelState.SERVING && totalSleepSeconds < 300);

                eventTrackerService.pushEvent(EventType.MODEL_TRAINED, String.format("Modelo %s entrenado exitosamente", modelName));

                session.setModelState(ModelState.SERVING);
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Unable submit event. "+ e.getMessage());
                e.printStackTrace();
            }
            return "FINISHED";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Model state result: "+ result);
            updateView();
            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Entrenando el modelo",
                    "Aguarde por favor... esta tarea puede demorar varios minutos");
        }

        @Override
        protected void onProgressUpdate(String... text) {}
    }
}
