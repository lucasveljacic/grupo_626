package org.activityrecognition.measure.client;

import retrofit2.converter.gson.GsonConverterFactory;

public class MeasureClientFactory {
    private static MeasureClient client;
    private static final String BASE_URL = "http://192.168.0.8:8080";

    public static MeasureClient getClient() {
        if (client == null) {
            client = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MeasureClient.class);
        }

        return client;
    }
}
