package com.catalpa.scheduler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;

public class CDVSchedulerPlugin extends CordovaPlugin {
    private boolean isForceReload = false;

    @Override
    protected void pluginInitialize() {
        Activity activity   = cordova.getActivity();
        Intent launchIntent = activity.getIntent();
        String action 		= launchIntent.getAction();

        if ((action != null) && (SchedulerPlugin.ACTION_FORCE_RELOAD.equalsIgnoreCase(action))) {
            isForceReload = true;
            activity.moveTaskToBack(true);
        }
    }

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        boolean result = false;
        if (SchedulerPlugin.ACTION_CONFIGURE.equalsIgnoreCase(action)) {
            result = true;
            configure(data.getJSONObject(0), callbackContext);
        } else if (SchedulerPlugin.ACTION_START.equalsIgnoreCase(action)) {
            result = true;
            start(callbackContext);
        } else if (SchedulerPlugin.ACTION_STOP.equalsIgnoreCase(action)) {
            result = true;
            stop(callbackContext);
        } else if (SchedulerPlugin.ACTION_STATUS.equalsIgnoreCase(action)) {
            result = true;
            callbackContext.success(getAdapter().status());
        } else if (SchedulerPlugin.ACTION_FINISH.equalsIgnoreCase(action)) {
            finish(callbackContext);
            result = true;
        }
        return result;
    }

    private void configure(JSONObject options, final CallbackContext callbackContext) throws JSONException {
        SchedulerPlugin adapter = getAdapter();

        SchedulerPluginConfig.Builder config = new SchedulerPluginConfig.Builder();
        if (options.has("minimumFetchInterval")) {
            config.setMinimumFetchInterval(options.getInt("minimumFetchInterval"));
        }
        SchedulerPlugin.Callback callback = new SchedulerPlugin.Callback() {
            @Override
            public void onFetch() {
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        };
        adapter.configure(config.build(), callback);
        if (isForceReload) {
            callback.onFetch();
        }
        isForceReload = false;
    }

    @TargetApi(21)
    private void start(CallbackContext callbackContext) {
        SchedulerPlugin adapter = getAdapter();
        adapter.start();
        callbackContext.success(adapter.status());
    }

    private void stop(CallbackContext callbackContext) {
        SchedulerPlugin adapter = getAdapter();
        adapter.stop();
        callbackContext.success();
    }

    private void finish(CallbackContext callbackContext) {
        SchedulerPlugin adapter = getAdapter();
        adapter.finish();
        callbackContext.success();
    }

    private SchedulerPlugin getAdapter() {
        return SchedulerPlugin.getInstance(cordova.getActivity().getApplicationContext());
    }
}
