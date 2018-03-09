package com.catalpa.scheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


public class SchedulerPluginConfig {
    private Builder config;

    private static final int MINIMUM_FETCH_INTERVAL = 1;

    public static class Builder {
        private int minimumFetchInterval = MINIMUM_FETCH_INTERVAL;
        private String jobService = null;

        public Builder setMinimumFetchInterval(int fetchInterval) {
            if (fetchInterval >= MINIMUM_FETCH_INTERVAL) {
                this.minimumFetchInterval = fetchInterval;
            }
            return this;
        }

        public Builder setJobService(String className) {
            this.jobService = className;
            return this;
        }

        public SchedulerPluginConfig build() {
            return new SchedulerPluginConfig(this);
        }

        public SchedulerPluginConfig load(Context context) {
            SharedPreferences preferences = context.getSharedPreferences(SchedulerPlugin.TAG, 0);
            if (preferences.contains("fetchInterval")) {
                setMinimumFetchInterval(preferences.getInt("fetchInterval", minimumFetchInterval));
            }
            if (preferences.contains("jobService")) {
                setJobService(preferences.getString("jobService", null));
            }
            return new SchedulerPluginConfig(this);
        }
    }

    private SchedulerPluginConfig(Builder builder) {
        config = builder;
    }

    public void save(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SchedulerPlugin.TAG, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("minimumFetchInterval", config.minimumFetchInterval);
        editor.putString("jobService", config.jobService);
        editor.apply();
    }

    public int getMinimumFetchInterval() {
        return config.minimumFetchInterval;
    }

    public String getJobService() { return config.jobService; }

    public String toString() {
        JSONObject output = new JSONObject();
        try {
            output.put("minimumFetchInterval", config.minimumFetchInterval);
            output.put("jobService", config.jobService);
            return output.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            return output.toString();
        }
    }
}
