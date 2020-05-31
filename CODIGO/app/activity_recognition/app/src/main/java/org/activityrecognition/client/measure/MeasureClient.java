package org.activityrecognition.client.measure;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MeasureClient {
        @POST("/measures")
        Call<Void> pushMeasures(@Body MeasureRequest measures);
}
