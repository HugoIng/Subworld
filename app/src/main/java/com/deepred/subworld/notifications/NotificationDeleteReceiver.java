package com.deepred.subworld.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by aplicaty on 25/02/16.
 */
public class NotificationDeleteReceiver extends BroadcastReceiver {
    public NotificationDeleteReceiver(){
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BaseNotificationBuilder.SOAPBOX_NOTIF_DELETED_EVENT)) {
            Class clazz = (Class)intent.getSerializableExtra(BaseNotificationBuilder.SOAPBOX_NOTIF_CLASS);
            NotificationGenerator.getInstance().clearNotifications(clazz);
        }
    }
}
