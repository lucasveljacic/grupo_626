package org.activityrecognition.external.client.model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ModelClient {

        @PUT("/models/{id}")
        Call<ModelDTO> create(@Path("id") String modelId);

        @GET("/models/{id}")
        Call<ModelDTO> get(@Path("id") String modelId);

        @DELETE("/models/{id}")
        Call<Void> delete(@Path("id") String modelId);

        @POST("/models/{id}/events/{event_id}")
        Call<EventResponseDTO> pushEvent(@Path("id") String modelId, @Path("event_id") String eventId);

        @POST("/models/{id}/measures/{user_id}")
        Call<Void> pushMeasures(@Path("id") String modelId,
                                @Path("user_id") String userId,
                                @Body MeasureRequest measures);

        @POST("/models/{id}/predictions")
        Call<PredictionOutputDTO> predict(@Path("id") String modelId,
                                          @Body PredictionInputDTO input);
}
