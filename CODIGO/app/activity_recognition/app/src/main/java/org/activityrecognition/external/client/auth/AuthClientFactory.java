package org.activityrecognition.external.client.auth;

import retrofit2.converter.gson.GsonConverterFactory;

public class AuthClientFactory {
    private static AuthClient client;
    private static final String BASE_URL = "http://so-unlam.net.ar/";

    public static AuthClient getClient() {
        if (client == null) {
            client = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AuthClient.class);
        }

        return client;
    }
}
