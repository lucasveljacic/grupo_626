package org.activityrecognition;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.activityrecognition.client.model.EventResponseDTO;
import org.activityrecognition.client.model.ModelClient;
import org.activityrecognition.client.model.ModelClientFactory;
import org.activityrecognition.client.model.ModelDTO;
import org.activityrecognition.client.model.ModelEvent;
import org.activityrecognition.client.model.ModelState;
import org.activityrecognition.user.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "ACTREC_BASEACTIVITY";
    private NetworkChangeReceiver mNetworkReceiver;
    private ModelClient modelClient;
    protected SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

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

    protected abstract void disableActions();
    protected abstract void updateView();

    protected ModelClient getModelClient() {
        if (modelClient == null) {
            modelClient = ModelClientFactory.getClient();
        }
        return modelClient;
    }

    protected void sendModelTransition(ModelEvent event) {
        disableActions();
        // launch a thread with the http call to the external service
        Call<EventResponseDTO> call = getModelClient().pushEvent(session.getModelName(), event.name());
        call.enqueue(new Callback<EventResponseDTO>() {
            @Override
            public void onResponse(Call<EventResponseDTO> call, Response<EventResponseDTO> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Event sent successfully!");
                    loadModelState();
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

    protected void loadModelState() {
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
