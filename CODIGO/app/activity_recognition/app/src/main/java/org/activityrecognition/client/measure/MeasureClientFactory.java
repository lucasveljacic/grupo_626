package org.activityrecognition.client.measure;

import retrofit2.converter.gson.GsonConverterFactory;

public class MeasureClientFactory {
    private static MeasureClient client;
    private static final String BASE_URL = "http://3.23.104.237:8080";

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
