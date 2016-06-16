package com.deepred.subworld.notifications;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by aplicaty on 25/02/16.
 */
public  interface IBaseBuilder {
    NotificationCompat.Builder getBuilder();
    PendingIntent getPendingIntent();
}
