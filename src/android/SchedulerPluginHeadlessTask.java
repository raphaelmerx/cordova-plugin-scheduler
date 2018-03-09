package com.catalpa.scheduler;

import android.content.Context;
import android.util.Log;


public class SchedulerPluginHeadlessTask implements HeadlessTask {
    @Override
    public void onFetch(Context context) {
        Log.d(SchedulerPlugin.TAG, "SchedulerPluginHeadlessTask onFetch -- DEFAULT IMPLEMENTATION");
        SchedulerPlugin.getInstance(context).finish();
    }
}
