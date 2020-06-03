package org.activityrecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.client.model.EventResponseDTO;
import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.client.model.ModelClientFactory;
import org.activityrecognition.client.model.ModelDTO;
import org.activityrecognition.client.model.ModelEvent;
import org.activityrecognition.client.model.ModelState;
import org.activityrecognition.event.EventTrackerService;
import org.activityrecognition.event.EventType;
import org.activityrecognition.user.SessionManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int COLLECT_TIME_SEC = 60;
    private final String TAG = "ACTREC_MENU";

    private SessionManager session;
    private Button collectUser1Button;
    private Button collectUser2Button;
    private Button trainButton;
    private Button predictButton;
    private Button resetButton;
    private Button logoutButton;
    private ModelClient modelClient;
    private EventTrackerService eventTrackerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

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

        if (session.getModelState() == null) {
            createModel();
        }

        updateView();

        eventTrackerService = new EventTrackerService(session);
    }

    private void createModel() {
        String modelName = session.getModelName();
        // launch a thread with the http call to the external service
        Call<ModelDTO> call = getModelClient().create(modelName);
        call.enqueue(new Callback<ModelDTO>() {
            @Override
            public void onResponse(Call<ModelDTO> call, Response<ModelDTO> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Model created successfully!");
                    session.setModelState(ModelState.NEW);
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
        // launch a thread with the http call to the external service
        Call<EventResponseDTO> call = getModelClient().pushEvent(session.getModelName(), ModelEvent.RESET.name());
        call.enqueue(new Callback<EventResponseDTO>() {
            @Override
            public void onResponse(Call<EventResponseDTO> call, Response<EventResponseDTO> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Model reset successfully!");
                    session.setModelState(ModelState.NEW);
                    updateView();
                } else {
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<EventResponseDTO> call, Throwable t) {
                Log.e(TAG, "Unable to submit post to API. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    private void startTraining() {
        TriggerTrainingAsyncTaskRunner runner = new TriggerTrainingAsyncTaskRunner();
        runner.execute();
    }

    private void collectUser1Metrics() {
        collectUser1Button.setEnabled(false);
        collectUserMetrics("1");
    }
    private void collectUser2Metrics() {
        collectUser2Button.setEnabled(false);
        collectUserMetrics("2");
    }

    private void collectUserMetrics(String id) {
        Intent intent = new Intent(getApplicationContext(), CollectActivity.class);
        intent.putExtra("USER_ID", id);
        intent.putExtra("COLLECTION_TIME_SEC", COLLECT_TIME_SEC);
        startActivity(intent);
    }

    private void startPrediction() {
        Intent intent = new Intent(getApplicationContext(), PredictActivity.class);
        startActivity(intent);
    }

    private void updateView() {
        ModelState modelState = session.getModelState();

        collectUser1Button.setEnabled(false);
        collectUser2Button.setEnabled(false);
        trainButton.setEnabled(false);
        predictButton.setEnabled(false);

        if (modelState == null) {
            return;
        }

        switch (modelState) {
            case NEW:
                collectUser1Button.setEnabled(true);
                break;
            case COLLECTED_1:
                collectUser2Button.setEnabled(true);
                break;
            case COLLECTED_2:
                trainButton.setEnabled(true);
                break;
            case SERVING:
                predictButton.setEnabled(true);
                break;
        }
    }

    private ModelClient getModelClient() {
        if (modelClient == null) {
            modelClient = ModelClientFactory.getClient();
        }
        return modelClient;
    }

    // inner class to handle training waiting
    private class TriggerTrainingAsyncTaskRunner extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            String modelName = session.getModelName();

            // launch a thread with the http call to the external service
            Call<EventResponseDTO> call = getModelClient().pushEvent(modelName, ModelEvent.START_TRAINING.name());
            try {
                Response<EventResponseDTO> response = call.execute();
                if (response.isSuccessful()) {
                    Log.i(TAG, String.format("Event %s sent successfully!", ModelEvent.START_TRAINING.name()));

                    Call<ModelDTO> callGet;
                    Response<ModelDTO> responseGet;
                    ModelState state = null;

                    int totalSleepSeconds = 0;
                    do {
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
                } else {
                    Log.e(TAG, response.message());
                }
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Unable submit event. "+ e.getMessage());
                e.printStackTrace();
            }
            return "FINISHED";
        }

        @Override
        protected void onPostExecute(String modelState) {
            Log.e(TAG, "Model state result: "+ modelState);
            updateView();
            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Entrenando el modelo",
                    "Aguarde por favor... esta tarea puede demorar unos minutos");
        }

        @Override
        protected void onProgressUpdate(String... text) {}
    }

}
