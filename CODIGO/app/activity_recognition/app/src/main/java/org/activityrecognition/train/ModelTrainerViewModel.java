package org.activityrecognition.train;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.core.event.EventTrackerService;
import org.activityrecognition.user.SessionManager;

public class ModelTrainerViewModel extends ViewModel implements ModelTrainer.Listener {

    private final ModelTrainer modelTrainer = new ModelTrainer();
    private MutableLiveData<Integer> progressPercentageLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isStartedLiveData = new MutableLiveData<>();

    private ModelTrainerViewModel(SessionManager sessionManager, EventTrackerService eventTrackerService, ModelClient client) {
        modelTrainer.setEventTrackerService(eventTrackerService);
        modelTrainer.setSession(sessionManager);
        modelTrainer.setClient(client);
        modelTrainer.setListener(this);
    }

    public void start() {
        if (modelTrainer.start()) {
            isStartedLiveData.setValue(true);
        }
    }

    @Override
    public void onStateUpdated(Integer progressPercentage) {
        // Using postValue() since it's called from a background thread.
        progressPercentageLiveData.postValue(progressPercentage);
    }

    public LiveData<Integer> getElapsedTime() {
        return progressPercentageLiveData;
    }

    public LiveData<Boolean> isStarted() {
        return isStartedLiveData;
    }

    public void finishTraining() {
        isStartedLiveData.setValue(false);
        modelTrainer.dispose();
    }

    @Override
    protected void onCleared() {
        modelTrainer.dispose();
        super.onCleared();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private SessionManager session;
        private EventTrackerService eventTrackerService;
        private ModelClient client;
        public Factory(SessionManager sessionManager, EventTrackerService eventTrackerService, ModelClient client) {
            this.session = sessionManager;
            this.eventTrackerService = eventTrackerService;
            this.client = client;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ModelTrainerViewModel(session, eventTrackerService, client);
        }
    }
}
