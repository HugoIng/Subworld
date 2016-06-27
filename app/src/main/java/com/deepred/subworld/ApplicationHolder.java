package com.deepred.subworld;

/**
 * Created by aplicaty on 25/02/16.
 */
public class ApplicationHolder {
    private static SubworldApplication app;

    public static SubworldApplication getApp() {
        return app;
    }

    public static void setApp(SubworldApplication app) {
        ApplicationHolder.app = app;
    }
}
