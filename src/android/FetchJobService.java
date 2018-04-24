package com.catalpa.scheduler;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


@TargetApi(21)
public class FetchJobService extends JobService {
    @Override
    public boolean onStartJob(final JobParameters params) {
        Context context = getApplicationContext();

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            Log.d(SchedulerPlugin.TAG, "no network available, noop job");
            return false;
        }

        CompletionHandler completionHandler = new CompletionHandler() {
            @Override
            public void finish() {
                Log.d(SchedulerPlugin.TAG, "- jobFinished");
                jobFinished(params, false);
            }
        };
        SchedulerPlugin.getInstance(context).onFetch(completionHandler);

        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        Log.d(SchedulerPlugin.TAG, "- onStopJob");
        jobFinished(params, false);
        return true;
    }

    public interface CompletionHandler {
        void finish();
    }
}
