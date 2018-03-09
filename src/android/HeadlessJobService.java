package com.catalpa.scheduler;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;


@TargetApi(21)
public class HeadlessJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {

        SchedulerPlugin adapter = SchedulerPlugin.getInstance(getApplicationContext());

        if (adapter.isMainActivityActive()) {
            return true;
        }

        adapter.registerCompletionHandler(new FetchJobService.CompletionHandler() {
            @Override
            public void finish() {
                Log.d(SchedulerPlugin.TAG, "HeadlessJobService jobFinished");
                jobFinished(params, false);
            }
        });

        Log.d(SchedulerPlugin.TAG, "HeadlessJobService onStartJob");
        new SchedulerPluginHeadlessTask().onFetch(getApplicationContext());
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(SchedulerPlugin.TAG, "JobService onStopJob");
        jobFinished(params, false);
        return true;
    }
}
