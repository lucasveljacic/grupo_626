package org.activityrecognition.client.user;

import retrofit2.converter.gson.GsonConverterFactory;

public class UserClientFactory {
    private static UserClient client;
    private static final String BASE_URL = "http://so-unlam.net.ar/";

    public static UserClient getClient() {
        if (client == null) {
            client = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(UserClient.class);
        }

        return client;
    }
}
