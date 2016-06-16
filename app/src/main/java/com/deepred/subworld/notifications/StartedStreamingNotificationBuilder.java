package com.deepred.subworld.notifications;

/**
 * Created by aplicaty on 25/02/16.
 */
public class StartedStreamingNotificationBuilder {
    public static final int NOTIFICATION_ID = 1001010;

    public StartedStreamingNotificationBuilder() {
        super();
    }

    //public void fireNotification(INewUpdatedContent content) {

        /*IBaseBuilder bb = getBaseBuilder();
        NotificationCompat.Builder builder = bb.getBuilder();
        builder.setSmallIcon(R.drawable.sw);
        builder.setLights(ApplicationHolder.getApp().getResources().getColor(R.color.colorPrimary), 300, 2000);

        synchronized (noticationStack) {
            try {

                if (!noticationStack.contains(content)) {
                    noticationStack.add(content);
                }

                // Creates an explicit intent for an Activity in your app
                Intent notificationIntent = new Intent(context, InitApplication.class);
                //notificationIntent.putExtra(SOAPBOX_NOTIF_CLASS, StartedBroadcastEventStreaming.class);

                // The stack builder object will contain an artificial back stack for the started Activity.
                // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(InitApplication.class);

                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(notificationIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);
                builder.setAutoCancel(true);

                int count = noticationStack.size();
                if (count == 1) {
                    // We just have one message to notify
                    StartedBroadcastEventStreaming evt = (StartedBroadcastEventStreaming)noticationStack.get(0);
                    com.aplicaty.soapbox.util.LogClass.logDebug(ILogsTags.NOTIFICATIONS, "just one streaming started: roomName:" + evt.getRoomName());
                    com.aplicaty.soapbox.util.LogClass.logDebug(ILogsTags.NOTIFICATIONS, "just one streaming started: streamType:" + evt.getStreamType());
                    com.aplicaty.soapbox.util.LogClass.logDebug(ILogsTags.NOTIFICATIONS, "just one streaming started: roomId:" + evt.getRoomId());

                    String title = context.getResources().getString(R.string.mensaje_inicio_streaming);
                    String visibleText = String.format(context.getResources().getString(R.string.send_streaming_start), ContactoAdapter.getInstance().getContact(evt.getUserId()).getContactName());

                    builder.setContentTitle(title);
                    if (visibleText != null && visibleText.trim().length() > 0) {
                        builder.setContentText(visibleText);
                    } else {
                        return;
                    }

                    notificationIntent.putExtra(SOAPBOX_NOTIF_ID, "" + evt.getRoomId());
                } else {
                    NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                    builder.setStyle(style);

                    notificationIntent.putExtra(SOAPBOX_NOTIF_ID, "X");
                    String title = context.getResources().getQuantityString(R.plurals.numberOfStreamings, count, count);
                    String visibleText = context.getResources().getString(R.string.streaming_invitation);

                    // We only expose a content for the compact view of the notification
                    builder.setContentTitle(title);
                    builder.setContentText(visibleText);
                }
                notificationManager.notify(NOTIFICATION_ID, builder.build());

                // Sound, vibrate??
               //if (UserPreferencesManager.getInstance().getBool(UserPreferencesManager.PREF_STREAMING_NOTIFICATION_VIBRATION_CHECK))
                    vibrate();
               if (UserPreferencesManager.getInstance().getBool(UserPreferencesManager.PREF_STREAMING_NOTIFICATION_RINGTONE_CHECK)) {
                    ring(UserPreferencesManager.getInstance().getString(UserPreferencesManager.PREF_STREAMING_NOTIFICATION_RINGTONE));
               }

            } catch (Exception e) {
                // If the received content has generated a eror building its notification
                // the we won't keep it to avoid having the same error further.
                noticationStack.remove(content);
            }
        }*/
    //}

    /*protected Class getSourceClass() {
        return StartedBroadcastEventStreaming.class;
    }*/
}
