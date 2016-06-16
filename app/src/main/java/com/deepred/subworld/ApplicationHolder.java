package com.deepred.subworld;

import com.deepred.subworld.SubworldApplication;
/**
 * Created by aplicaty on 25/02/16.
 */
public class ApplicationHolder {
    private static SubworldApplication app;

    public static SubworldApplication getApp() {
        return app;
    }

    public static void setAplicatyApplication(SubworldApplication app) {
        ApplicationHolder.app = app;
    }
}
