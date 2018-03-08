package com.transistorsoft.cordova.backgroundfetch;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class HeadlessBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BackgroundFetch adapter = BackgroundFetch.getInstance(context.getApplicationContext());
        if (adapter.isMainActivityActive()) {
            return;
        }
        Log.d(BackgroundFetch.TAG, "HeadlessBroadcastReceiver onReceive");
        new BackgroundFetchHeadlessTask().onFetch(context.getApplicationContext());
    }
}
