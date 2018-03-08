package com.transistorsoft.cordova.backgroundfetch;

import android.content.Context;
import android.util.Log;


public class BackgroundFetchHeadlessTask implements HeadlessTask {
    @Override
    public void onFetch(Context context) {
        Log.d(BackgroundFetch.TAG, "BackgroundFetchHeadlessTask onFetch -- DEFAULT IMPLEMENTATION");
        BackgroundFetch.getInstance(context).finish();
    }
}
