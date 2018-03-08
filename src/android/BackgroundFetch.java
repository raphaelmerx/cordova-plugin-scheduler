package com.transistorsoft.cordova.backgroundfetch;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class BackgroundFetch {
    public static final String TAG = "TSBackgroundFetch";

    public static final String ACTION_CONFIGURE = "configure";
    public static final String ACTION_START     = "start";
    public static final String ACTION_STOP      = "stop";
    public static final String ACTION_FINISH    = "finish";
    public static final String ACTION_STATUS    = "status";
    public static final String ACTION_FORCE_RELOAD = TAG + "-forceReload";

    public static final String EVENT_FETCH      = ".event.BACKGROUND_FETCH";

    public static final int STATUS_AVAILABLE = 2;

    private static BackgroundFetch mInstance = null;
    private static int FETCH_JOB_ID = 999;

    public static BackgroundFetch getInstance(Context context) {
        if (mInstance == null) {
            mInstance = getInstanceSynchronized(context.getApplicationContext());
        }
        return mInstance;
    }

    private static synchronized BackgroundFetch getInstanceSynchronized(Context context) {
        if (mInstance == null) mInstance = new BackgroundFetch(context.getApplicationContext());
        return mInstance;
    }

    private Context mContext;
    private BackgroundFetch.Callback mCallback;
    private BackgroundFetchConfig mConfig;
    private FetchJobService.CompletionHandler mCompletionHandler;

    private BackgroundFetch(Context context) {
        mContext = context;
    }

    public void configure(BackgroundFetchConfig config, BackgroundFetch.Callback callback) {
        Log.d(TAG, "- configure: " + config);
        mCallback = callback;
        config.save(mContext);
        mConfig = config;
        start();
    }

    public void onBoot() {
        mConfig = new BackgroundFetchConfig.Builder().load(mContext);
        start();
    }

    @TargetApi(21)
    public void start() {
        Log.d(TAG, "- start scheduling job");
        long fetchInterval = mConfig.getMinimumFetchInterval() * 60L * 1000L;
        JobScheduler jobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(FETCH_JOB_ID, new ComponentName(mContext, FetchJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .setPersisted(true);
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            builder.setPeriodic(fetchInterval, fetchInterval);
        } else {
            builder.setPeriodic(fetchInterval);
        }
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    public void stop() {
        Log.d(TAG,"- stop");

        if (mCompletionHandler != null) {
            mCompletionHandler.finish();
        }
        JobScheduler jobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.cancel(FETCH_JOB_ID);
        }
    }

    public void finish() {
        Log.d(TAG, "- finish");
        if (mCompletionHandler != null) {
            mCompletionHandler.finish();
            mCompletionHandler = null;
        }
    }

    public int status() {
        return STATUS_AVAILABLE;
    }

    /**
     * Used for Headless operation for registering completion-handler to execute #jobFinised on JobScheduler
     * @param completionHandler
     */
    public void registerCompletionHandler(FetchJobService.CompletionHandler completionHandler) {
        mCompletionHandler = completionHandler;
    }

    public void onFetch(FetchJobService.CompletionHandler completionHandler) {
        mCompletionHandler = completionHandler;
        onFetch();
    }

    public void onFetch() {
        Log.d(TAG, "- Background Fetch event received");
        if (mConfig == null) {
            mConfig = new BackgroundFetchConfig.Builder().load(mContext);
        }
        if (isMainActivityActive()) {
            if (mCallback != null) {
                mCallback.onFetch();
            }
        } else {
            Log.d(TAG, "- MainActivity is inactive");
            forceMainActivityReload();
        }
    }

    public void forceMainActivityReload() {
        Log.i(TAG,"- Forcing MainActivity reload");
        PackageManager pm = mContext.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(mContext.getPackageName());
        if (launchIntent == null) {
            Log.w(TAG, "- forceMainActivityReload failed to find launchIntent");
            return;
        }
        launchIntent.setAction(ACTION_FORCE_RELOAD);
        launchIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        mContext.startActivity(launchIntent);
    }

    public Boolean isMainActivityActive() {
        Boolean isActive = false;

        if (mContext == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
            for (ActivityManager.RunningTaskInfo task : tasks) {
                if (mContext.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
                    isActive = true;
                    break;
                }
            }
        } catch (java.lang.SecurityException e) {
            Log.w(TAG, "TSBackgroundFetch attempted to determine if MainActivity is active but was stopped due to a missing permission.  Please add the permission 'android.permission.GET_TASKS' to your AndroidManifest.  See Installation steps for more information");
            throw e;
        }
        return isActive;
    }

    /**
     * @interface BackgroundFetch.Callback
     */
    public interface Callback {
        void onFetch();
    }
}
