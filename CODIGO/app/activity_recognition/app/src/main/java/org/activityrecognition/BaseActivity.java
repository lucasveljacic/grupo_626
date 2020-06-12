package org.activityrecognition;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "ACTREC_BASEACTIVITY";
    private NetworkChangeReceiver mNetworkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
