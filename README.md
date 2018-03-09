# Cordova plugin scheduler

A Cordova plugin that schedules periodic Android background tasks using the [`JobScheduler`](https://developer.android.com/reference/android/app/job/JobScheduler.html).

## Installation

```bash
   $ cordova plugin add --save cordova-plugin-scheduler
```

## Usage

To schedule a job:
```javascript
window.SchedulerPlugin.configure(
    fetchTask,
    errorHandler,
    { minimumFetchInterval: i }  // i in minutes
);
```

`fetchTask` should call `SchedulerPlugin.finish()` to release the Android wakelock.

## Example

```javascript

var fetchTask = function() {
    codePush.sync(null);
    window.SchedulerPlugin.finish();
};

var errorHandler = function(error) {
    console.log('SchedulerPlugin error: ', error);
};

window.SchedulerPlugin.configure(
    fetchTask,
    errorHandler,
    { minimumFetchInterval: 60 }  // run every hour
);
```

## Debugging

- Observe the plugin logs:
```bash
$ adb logcat -s SchedulerPluginTag
```
- Simulate a background-fetch event on a device (only works for Android >=7.0):
```bash
$ adb shell cmd jobscheduler run -f <your.application.id> 999
```
- See all scheduled jobs on your phone (only works for Android >=7.0)
```bash
$ adb shell dumpsys jobscheduler
```

## Notes

* The same [jobId](https://developer.android.com/reference/android/app/job/JobInfo.Builder.html) (999) is used every time a job is scheduled, so former fetch handlers are overriden when you define a new fetch handler.
* The job is persisted on device reboots.
* `SchedulerPlugin.finish()` will call [`JobService.jobFinished`](https://developer.android.com/reference/android/app/job/JobService.html#jobFinished).

## Credits

This plugin is mostly a merge of https://github.com/transistorsoft/cordova-plugin-background-fetch and https://github.com/transistorsoft/transistor-background-fetch, with extra built-in job parameters.
