package com.alonsotagle.nanodegree;

import android.app.ActivityManager;
import android.content.Context;
import java.util.List;

/**
 * Created by AlonsoTagle on 09/08/15.
 */
public class Application extends android.app.Application {

    private static Context context;
    public static boolean isPlayingNow;
    public static String currentTrackUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        isPlayingNow = false;
    }

    public static boolean getIsPlayingNow()
    {
        return isPlayingNow;
    }

    public static void setIsPlayingNow(boolean playingNow)
    {
        isPlayingNow = playingNow;
    }

    public static String getCurrentTrackUrl()
    {
        return currentTrackUrl;
    }

    public static void setCurrentTrackUrl(String trackUrl)
    {
        currentTrackUrl = trackUrl;
    }

    public static Context getContext(){
        return context;
    }

    public static boolean isLastActivity(String activityClassName){
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(20);
        String packageName = context.getPackageName();

        for(ActivityManager.RunningTaskInfo taskInfo : taskList) {

            if(taskInfo.baseActivity.getPackageName().equals(packageName)) {
                return taskInfo.numActivities == 1 && taskInfo.topActivity.getClassName().equals(activityClassName);
            }
        }

        return false;
    }

    public static boolean isAllActivityClosed() {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(20);
        String packageName = context.getPackageName();

        for(ActivityManager.RunningTaskInfo taskInfo : taskList) {
            if(taskInfo.baseActivity.getPackageName().equals(packageName)) {
                return false;
            }
        }

        return true;
    }
}