package org.activityrecognition.ui.train;

import android.util.Log;

import androidx.annotation.Nullable;

import org.activityrecognition.external.client.model.EventResponseDTO;
import org.activityrecognition.external.client.model.ModelClient;
import org.activityrecognition.external.client.model.ModelDTO;
import org.activityrecognition.external.client.model.ModelEvent;
import org.activityrecognition.external.client.model.ModelState;
import org.activityrecognition.core.event.EventTrackerService;
import org.activityrecognition.ui.user.SessionManager;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

public class ModelTrainer {

    private Integer progressPercentage = 0;

    interface Listener {
        void onStateUpdated(Integer progressPercentage);
    }

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private static final String TAG = "ACTREC_MODELTRAINER";
    private boolean isStarted;

    private SessionManager session;
    private ModelClient client;

    public void setSession(SessionManager session) {
        this.session = session;
    }

    public void setClient(ModelClient client) {
        this.client = client;
    }

    @Nullable
    private volatile Listener listener;

    @Nullable
    private ScheduledFuture<?> scheduledFuture;

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;

        if (listener != null) {
            listener.onStateUpdated(progressPercentage);
        }
    }

    public synchronized boolean start() {
        Listener listener = ModelTrainer.this.listener;
        if (listener != null) {
            listener.onStateUpdated(progressPercentage);
        }

        if (isStarted) {
            return false;
        }

        isStarted = true;
        startTask();
        return true;
    }

    private void startTask() {
        scheduledFuture = executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                synchronized (ModelTrainer.this) {

                    try {
                        // Only if model is in state COLLECTED_2, we send a event of type START_TRAINING
                        if (session.getModelState() == ModelState.COLLECTED_2) {
                            Response<EventResponseDTO> response = client
                                    .pushEvent(session.getModelName(), ModelEvent.START_TRAINING.name())
                                    .execute();

                            if (response.isSuccessful()) {
                                Log.i(TAG, String.format("Event %s sent successfully!", ModelEvent.START_TRAINING.name()));
                                session.setModelState(ModelState.TRAINING);
                            } else {
                                Log.e(TAG, response.message());
                                return;
                            }
                        }

                        // Loading model state from server
                        loadModelStateSync();

                        recomputeProgress();
                    } catch (IOException e) {
                        Log.e(TAG, "Unable submit event. "+ e.getMessage());
                        e.printStackTrace();
                    }
                }

                Listener listener = ModelTrainer.this.listener;
                if (listener != null) {
                    listener.onStateUpdated(progressPercentage);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void dispose() {
        isStarted = false;
        executorService.shutdown();
    }

    protected void loadModelStateSync() {
        try {
            Response<ModelDTO> response = client.get(session.getModelName()).execute();
            if (response.isSuccessful()) {
                ModelState state = response.body().getState();
                if (state != null) {
                    Log.i(TAG, String.format("Loaded model state: %s", state));
                    session.setModelState(state);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recomputeProgress() {
        if (session.getModelState() == ModelState.SERVING) {
            progressPercentage = 100;
            return;
        }

        Integer threshold = 100;
        if (session.getModelState() == ModelState.TRAINING) {
            threshold = 60;
        } else if (session.getModelState() == ModelState.READY_TO_SERVE) {
            threshold = 100;
        }

        progressPercentage = progressPercentage + (threshold - progressPercentage) / 4;
    }
}
