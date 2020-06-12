package org.activityrecognition.client.model;

import retrofit2.converter.gson.GsonConverterFactory;

public class ModelClientFactory {
    private static ModelClient client;
    //private static final String BASE_URL = "http://18.221.185.88:8080";
    private static final String BASE_URL = "http://192.168.0.8:8080";

    public static ModelClient getClient() {
        if (client == null) {
            client = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ModelClient.class);
        }

        return client;
    }
}
