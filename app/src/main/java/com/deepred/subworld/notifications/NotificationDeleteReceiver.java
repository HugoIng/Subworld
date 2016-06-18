package com.deepred.subworld.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *
 */
public class NotificationDeleteReceiver extends BroadcastReceiver {
    public NotificationDeleteReceiver(){
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BaseNotificationBuilder.SUBWORLD_NOTIF_DELETED_EVENT)) {
            Class clazz = (Class)intent.getSerializableExtra(BaseNotificationBuilder.SUBWORLD_NOTIF_CLASS);
            NotificationGenerator.getInstance().clearNotifications(clazz);
        }
    }
}
