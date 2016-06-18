package com.deepred.subworld.notifications;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;

/**
 *
 */
public  interface IBaseBuilder {
    NotificationCompat.Builder getBuilder();
    PendingIntent getPendingIntent();
}
