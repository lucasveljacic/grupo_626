package org.activityrecognition.measure;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.Nullable;

import org.activityrecognition.client.measure.MeasureClient;
import org.activityrecognition.client.measure.MeasureClientFactory;
import org.activityrecognition.client.measure.MeasureRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeasurePublisherService extends IntentService implements PacketListener {

    MeasureClient client;
    private final String TAG = "MeasurePublisherService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MeasurePublisherService(String name) {
        super(name);
    }

    public MeasurePublisherService() {
        this("MeasurePublisherService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        SensorPacketCollector sensorPacketBuilder = new SensorPacketCollector(sensorManager);
        sensorPacketBuilder.start();

        client = MeasureClientFactory.getClient();

        sensorPacketBuilder.registerListener(this);
    }

    @Override
    public void onPackageComplete(List<String> packet) {
        // launch a thread with the http call to the external service
        MeasureRequest request = new MeasureRequest(packet);
        Call<Void> call = client.pushMeasures(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "packet pushed successfully!");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Unable to submit post to API. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }
}
