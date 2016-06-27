package com.deepred.subworld;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;

import com.deepred.subworld.service.LocationService;

/**
 * Created by aplicaty on 25/02/16.
 */
public class ApplicationLifecycleHandler implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private static final String TAG = ApplicationLifecycleHandler.class.getSimpleName();
    private static Object lock = new Object();
    private static volatile ApplicationLifecycleHandler instance = null;

    private boolean isAppInBackgroundState; // Real app status Foreground / Background
    private boolean screenOff; // Device screen is on/off

    private ApplicationLifecycleHandler() {
        isAppInBackgroundState = true;
        screenOff = false;
    }

    public static ApplicationLifecycleHandler getInstance() {
        ApplicationLifecycleHandler localInstance = instance;
        if (localInstance == null) {
            synchronized (lock) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ApplicationLifecycleHandler();
                }
            }
        }
        return localInstance;
    }

    /*
    Exported public method to be used by the app to know whether we are in FOREGROUND or BACKGROUND
     */
    public boolean isAppInBackground() {
        return (isAppInBackgroundState || screenOff);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isAppInBackgroundState) {
            setIsAppInBackground(false);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(int i) {
        if (i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            setIsAppInBackground(true);
        }
    }

    private void setIsAppInBackground(boolean state) {
        isAppInBackgroundState = state;
        SubworldApplication app = ApplicationHolder.getApp();
        if (app != null) {
            sendAppInBackgroundStatus(isAppInBackgroundState);
        }
    }

    private void sendAppInBackgroundStatus(boolean status) {
        //GameService.getInstance().changeBackgroundState(status);

        boolean useGPS = !status;

        LocationService serv = ApplicationHolder.getApp().getLocationService();
        if(serv != null)
            serv.switchProvider(useGPS);
        else
            LocationService.setProvider(useGPS);
    }

    public void updateScreenStatus(boolean screenState) {
        screenOff = screenState;
        if (!isAppInBackgroundState) {
            SubworldApplication app = ApplicationHolder.getApp();
            if (app != null) {
                sendAppInBackgroundStatus(screenOff);
            }
        }
    }
}
