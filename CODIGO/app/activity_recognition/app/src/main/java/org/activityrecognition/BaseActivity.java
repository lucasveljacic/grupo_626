package org.activityrecognition;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.external.client.model.EventResponseDTO;
import org.activityrecognition.external.client.model.ModelClient;
import org.activityrecognition.external.client.model.ModelClientFactory;
import org.activityrecognition.external.client.model.ModelDTO;
import org.activityrecognition.external.client.model.ModelEvent;
import org.activityrecognition.external.client.model.ModelState;
import org.activityrecognition.core.NetworkChangeReceiver;
import org.activityrecognition.ui.user.SessionManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {

    private static String TAG;
    private NetworkChangeReceiver mNetworkReceiver;
    private ModelClient modelClient;
    protected SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "ACTREC_" + getClass().getName().toUpperCase();

        Log.d(TAG, "performing onCreate()");

        session = SessionManager.getInstance(getApplicationContext());

        mNetworkReceiver = new NetworkChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
        Log.d(TAG, "performing onDestroy()");
    }

    protected boolean isOffline() {
        if (!mNetworkReceiver.isOnline()) {
            Log.i(TAG, "No internet connection!");

            Toast.makeText(getBaseContext(),
                    "No esta conectado a internet!",
                    Toast.LENGTH_LONG).show();
        }
        return !mNetworkReceiver.isOnline();
    }

    protected abstract void disableControls();
    protected abstract void updateView();

    protected ModelClient getModelClient() {
        if (modelClient == null) {
            modelClient = ModelClientFactory.getClient();
        }
        return modelClient;
    }

    protected void sendModelTransition(ModelEvent event) {
        disableControls();

        // launch a thread with the http call to the external service
        getModelClient()
                .pushEvent(session.getModelName(), event.name())
                .enqueue(new Callback<EventResponseDTO>() {
                    @Override
                    public void onResponse(Call<EventResponseDTO> call, Response<EventResponseDTO> response) {
                        if (response.isSuccessful()) {
                            Log.i(TAG, String.format("Event %s sent successfully!", event.name()));
                            session.setModelState(response.body().getModel().getState());
                            updateView();
                        } else {
                            Log.e(TAG, response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<EventResponseDTO> call, Throwable t) {
                        Log.e(TAG, "Unable to submit post to API. "+ t.getMessage());
                        t.printStackTrace();
                    }
                });
    }

    protected void loadModelStateAsync() {
        getModelClient().get(session.getModelName()).enqueue(new Callback<ModelDTO>() {
            @Override
            public void onResponse(Call<ModelDTO> call, Response<ModelDTO> response) {
                if (response.isSuccessful()) {
                    ModelState state = response.body().getState();
                    if (state != null) {
                        Log.i(TAG, String.format("Loaded model state: %s", state));
                        session.setModelState(state);
                        updateView();
                    }
                } else {
                    Log.e(TAG, "Unable to load model state!");
                }
            }

            @Override
            public void onFailure(Call<ModelDTO> call, Throwable t) {
                Log.e(TAG, "Unable to load model state. "+ t.getMessage());
                t.printStackTrace();
            }
        });
    }

    protected void loadModelStateSync() {
        try {
            Response<ModelDTO> response = getModelClient().get(session.getModelName()).execute();
            if (response.isSuccessful()) {
                ModelState state = response.body().getState();
                if (state != null) {
                    Log.i(TAG, String.format("Loaded model state: %s", state));
                    session.setModelState(state);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "performing onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "performing onResume()");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "performing onRestart()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "performing onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "performing onStop()");
    }
}
