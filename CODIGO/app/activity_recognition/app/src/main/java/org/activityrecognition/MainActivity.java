package org.activityrecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.activityrecognition.core.event.EventTrackerService;
import org.activityrecognition.core.event.EventType;
import org.activityrecognition.external.client.model.ModelDTO;
import org.activityrecognition.external.client.model.ModelEvent;
import org.activityrecognition.external.client.model.ModelState;
import org.activityrecognition.ui.collect.CollectActivity;
import org.activityrecognition.ui.predict.LastPredictionsActivity;
import org.activityrecognition.ui.predict.PredictActivity;
import org.activityrecognition.ui.train.ModelTrainerViewModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {
    private static final int COLLECTION_MAX_PACKS = 60;
    private final String TAG = "ACTREC_MENU";

    private Button collectUser1Button;
    private Button collectUser2Button;
    private Button trainButton;
    private Button predictButton;
    private Button predictionListButton;
    private Button resetButton;
    private Button logoutButton;
    private EventTrackerService eventTrackerService;

    private ModelTrainerViewModel trainingViewModel;

    ProgressDialog trainingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "performing onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session.checkLogin();

        trainingProgressDialog = new ProgressDialog(this);

        collectUser1Button = findViewById(R.id.btn_collect_user_1);
        collectUser2Button = findViewById(R.id.btn_collect_user_2);
        resetButton = findViewById(R.id.btn_reset);
        logoutButton = findViewById(R.id.btn_loguot);
        trainButton = findViewById(R.id.btn_train);
        predictButton = findViewById(R.id.btn_predict);
        predictionListButton = findViewById(R.id.btn_prediction_list);

        collectUser1Button.setOnClickListener(v -> collectUser1Metrics());
        collectUser2Button.setOnClickListener(v -> collectUser2Metrics());
        predictButton.setOnClickListener(v -> startPrediction());
        trainButton.setOnClickListener(v -> handleTraining());
        resetButton.setOnClickListener(v -> resetModel());
        logoutButton.setOnClickListener(v -> logout());
        predictionListButton.setOnClickListener(v -> goToPredictionList());

        eventTrackerService = new EventTrackerService(session);

        ModelTrainerViewModel.Factory factory = new ModelTrainerViewModel.Factory(session, getModelClient());
        trainingViewModel = new ViewModelProvider(getViewModelStore(), factory)
                .get(ModelTrainerViewModel.class);

        observeProgressPercentage();

        // this must go at last as it needs view objects to be loaded
        refreshModelState();
    }

    private void goToPredictionList() {
        Intent intent = new Intent(getApplicationContext(), LastPredictionsActivity.class);
        startActivity(intent);
    }

    protected void refreshModelState() {
        getModelClient().get(session.getModelName()).enqueue(new Callback<ModelDTO>() {
            @Override
            public void onResponse(Call<ModelDTO> call, Response<ModelDTO> response) {
                if (response.isSuccessful()) {
                    ModelState state = response.body().getState();
                    if (state != null) {
                        Log.i(TAG, String.format("Loaded model state: %s", state));
                        session.setModelState(state);
                        updateView();
                    }
                } else if (response.code() == 404) {
                    createModel();
                } else {
                    Log.e(TAG, String.format("Unable to load model state! Response code: %d", response.code()));
                }
            }

            @Override
            public void onFailure(Call<ModelDTO> call, Throwable t) {
                Log.e(TAG, "Unable to load model state. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void observeProgressPercentage() {
        trainingViewModel.getProgressPercentage().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer progress) {
                trainingProgressDialog.setProgress(progress);
                if (progress == 100) {
                    trainingViewModel.finishTraining();
                    trainingProgressDialog.dismiss();
                    updateView();
                    eventTrackerService.pushEvent(
                            EventType.MODEL_TRAINED,
                            String.format("Modelo %s entrenado exitosamente", session.getModelName()));
                }
            }
        });
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
        if (isOffline()) {
            return;
        }
        session.logoutUser();
    }

    private void resetModel() {
        if (isOffline()) {
            return;
        }
        sendModelTransition(ModelEvent.RESET);

        // cleaning session data
        session.setLastPredictions(new ArrayList<>());
        session.setSentDataPackets(0);
    }

    private void handleTraining() {
        if (isOffline()) {
            return;
        }
        trainingViewModel.start();
        showTrainingProgressBar();
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

    private void showTrainingProgressBar() {
        trainingProgressDialog.setTitle("Entrenando el modelo");
        trainingProgressDialog.setMessage("Esta tarea puede demorar varios minutos ...");
        trainingProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        trainingProgressDialog.setCancelable(false);
        trainingProgressDialog.setMax(100);
        trainingProgressDialog.show();
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
        predictionListButton.setEnabled(false);
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
                predictionListButton.setEnabled(true);
                break;
        }
    }
}
