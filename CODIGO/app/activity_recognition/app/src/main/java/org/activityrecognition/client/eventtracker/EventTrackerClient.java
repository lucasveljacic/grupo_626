package org.activityrecognition.client.eventtracker;

import org.activityrecognition.client.model.EventResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface EventTrackerClient {
        @POST("/api/api/event")
        Call<EventResponseDTO> pushEvent(@Header("token") String token, @Body EventDTO eventDTO);
}
