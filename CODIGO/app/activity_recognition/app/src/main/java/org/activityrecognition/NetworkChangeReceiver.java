package org.activityrecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class NetworkChangeReceiver extends BroadcastReceiver {

    private boolean isOnline = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if ( activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            isOnline = true;
        } else {
            isOnline = false;
        }
    }

    public boolean isOnline() {
        return isOnline;
    }
}
