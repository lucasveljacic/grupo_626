package org.activityrecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.activityrecognition.client.model.EventResponseDTO;
import org.activityrecognition.client.model.ModelDTO;
import org.activityrecognition.client.model.ModelEvent;
import org.activityrecognition.client.model.ModelState;
import org.activityrecognition.event.EventTrackerService;
import org.activityrecognition.event.EventType;
import org.activityrecognition.user.LoginActivity;

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
        trainButton.setOnClickListener(v -> handleTraining());
        resetButton.setOnClickListener(v -> resetModel());
        logoutButton.setOnClickListener(v -> logout());

        loadModelStateAsync();
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void createModel() {
        disableControls();
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

    //TriggerTrainingAsyncTaskRunner runner;

    private void handleTraining() {
        if (isOffline()) {
            return;
        }

        disableControls();

        if (session.getModelState() == ModelState.COLLECTED_2) {
                getModelClient().pushEvent(session.getModelName(), ModelEvent.START_TRAINING.name())
                    .enqueue(new Callback<EventResponseDTO>() {
                        @Override
                        public void onResponse(Call<EventResponseDTO> call, Response<EventResponseDTO> response) {
                            if (response.isSuccessful()) {
                                session.setModelState(ModelState.TRAINING);
                                Log.e(TAG, response.message());
                                pollUntilModelIsServing();
                            } else {
                                Log.e(TAG, "Unable to load model state!");
                            }
                        }
                        @Override
                        public void onFailure(Call<EventResponseDTO> call, Throwable t) {
                            Log.e(TAG, "Unable to load model state. "+ t.getMessage());
                            t.printStackTrace();
                        }
                    });
        } else {
            pollUntilModelIsServing();
        }



        //runner = new TriggerTrainingAsyncTaskRunner();
        //runner.execute();
    }

    private void pollUntilModelIsServing() {
        Toast.makeText(getBaseContext(), "Aguarde por favor... esta tarea puede demorar varios minutos. Será notificado cuando finalice.", Toast.LENGTH_LONG).show();
        waitUntilModelServing();
    }

    private void waitUntilModelServing() {
        getModelClient().get(session.getModelName()).enqueue(new Callback<ModelDTO>() {
            @Override
            public void onResponse(Call<ModelDTO> call, Response<ModelDTO> response) {
                if (response.isSuccessful()) {
                    ModelState state = response.body().getState();
                    if (state != null) {
                        Log.i(TAG, String.format("Model state: %s", state));
                        if (state != ModelState.SERVING) {
                            try {
                                Thread.sleep(5000);
                                waitUntilModelServing();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            session.setModelState(state);
                            Toast.makeText(getBaseContext(), "Entrenamiento finalizado!", Toast.LENGTH_LONG).show();
                            updateView();
                        }
                    }
                } else {
                    Log.e(TAG, "Unable to load model state!");
                }
            }

            @Override
            public void onFailure(Call<ModelDTO> call, Throwable t) {
                Log.e(TAG, "Unable to load model state. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }


    private void collectUser1Metrics() {
        if (isOffline()) {
            return;
        }
        disableControls();
        collectUserMetrics("1");
    }
    private void collectUser2Metrics() {
        if (isOffline()) {
            return;
        }
        disableControls();
        collectUserMetrics("2");
    }

    private void collectUserMetrics(String id) {
        disableControls();
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
    protected void disableControls() {
        collectUser1Button.setEnabled(false);
        collectUser2Button.setEnabled(false);
        trainButton.setEnabled(false);
        predictButton.setEnabled(false);
    }

    @Override
    protected void updateView() {
        disableControls();
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
                handleTraining();
                break;
            case SERVING:
                predictButton.setEnabled(true);
                break;
        }
    }

    // inner class to handle training and polling until finish
    private class TriggerTrainingAsyncTaskRunner extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            String modelName = session.getModelName();

            try {
                // Only if model is in state COLLECTED_2, we send a event of type START_TRAINING
                if (session.getModelState() == ModelState.COLLECTED_2) {
                    Call<EventResponseDTO> call = getModelClient().pushEvent(modelName, ModelEvent.START_TRAINING.name());
                    Response<EventResponseDTO> response = call.execute();
                    if (!response.isSuccessful()) {
                        Log.e(TAG, response.message());
                        return "FAILURE";
                    } else {
                        Log.i(TAG, String.format("Event %s sent successfully!", ModelEvent.START_TRAINING.name()));
                        session.setModelState(ModelState.TRAINING);
                    }
                }

                // start polling for training completion
                int totalSleepSeconds = 0;
                do {
                    if (isOffline()) {
                        return "ERROR";
                    }

                    // inside this method the state of model is loaded
                    loadModelStateSync();

                    Thread.sleep(5 * 1000);
                    totalSleepSeconds += 5;
                } while (session.getModelState() != ModelState.SERVING && totalSleepSeconds < 300);

                eventTrackerService.pushEvent(EventType.MODEL_TRAINED, String.format("Modelo %s entrenado exitosamente", modelName));
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Unable submit event. "+ e.getMessage());
                e.printStackTrace();
            }
            return "FINISHED";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "Model state result: "+ result);
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
