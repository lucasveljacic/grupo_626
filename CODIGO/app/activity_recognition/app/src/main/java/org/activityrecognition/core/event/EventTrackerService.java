package org.activityrecognition.core.event;

import android.util.Log;

import org.activityrecognition.client.eventtracker.EventDTO;
import org.activityrecognition.client.eventtracker.EventTrackerClient;
import org.activityrecognition.client.eventtracker.EventTrackerFactory;
import org.activityrecognition.client.model.EventResponseDTO;
import org.activityrecognition.user.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventTrackerService {
    private final String TAG = "ACTREC_EVENT_TRACKER";

    EventTrackerClient eventTrackerClient;
    SessionManager session;

    public EventTrackerService(SessionManager session) {
        this.session = session;
    }

    private EventTrackerClient getEvenTrackerClient() {
        if (eventTrackerClient == null) {
            eventTrackerClient = EventTrackerFactory.getClient();
        }
        return eventTrackerClient;
    }

    public void pushEvent(EventType eventType, String message) {

        // launch a thread with the http call to the external service
        EventDTO eventDTO = new EventDTO("DEV", eventType.name(), "ACTIVO", message);

        Call<EventResponseDTO> call = getEvenTrackerClient().pushEvent(
                session.getUserDetails().get(SessionManager.KEY_TOKEN), eventDTO);
        call.enqueue(new Callback<EventResponseDTO>() {
            @Override
            public void onResponse(Call<EventResponseDTO> call, Response<EventResponseDTO> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, String.format("Event %s sent successfully!", eventDTO.toString()));
                } else {
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<EventResponseDTO> call, Throwable t) {
                Log.e(TAG, "Unable to Push event to API. "+ t.getMessage());
                t.printStackTrace();
            }
        });

    }
}
