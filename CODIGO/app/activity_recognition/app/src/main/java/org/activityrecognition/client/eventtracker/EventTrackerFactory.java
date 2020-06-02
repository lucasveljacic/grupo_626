package org.activityrecognition.client.eventtracker;

import org.activityrecognition.client.eventtracker.EventTrackerClient;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventTrackerFactory {
    private static EventTrackerClient client;
    private static final String BASE_URL = "http://so-unlam.net.ar/";

    public static EventTrackerClient getClient() {

        if (client == null) {
            client = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getRequestHeader())
                    .build()
                    .create(EventTrackerClient.class);
        }

        return client;
    }
    private static OkHttpClient getRequestHeader() {
        return new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }
}
