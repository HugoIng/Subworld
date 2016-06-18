package com.deepred.subworld.notifications;

import java.util.Hashtable;

/**
 *
 */
public class NotificationGenerator {

    private static NotificationGenerator instance = new NotificationGenerator();
    private static Hashtable<Class, INotificationBuilder> builders;

    static {
        builders = new Hashtable<Class, INotificationBuilder>();
        try{
            //builders.put(StartedBroadcastEventStreaming.class, new StartedStreamingNotificationBuilder());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private NotificationGenerator(){

    }

    public static NotificationGenerator getInstance(){
        return instance;
    }

    /**
     * Delete all the previously sent notifications
     * @param c the class of notification to clear
     */
    public void clearNotifications(Class c){
        /*try {
            if (builders.containsKey(c)) {
                builders.get(c).clearNotifications();
            }
            android.app.NotificationManager notificationManager = (android.app.NotificationManager) ApplicationHolder.getApp().getSystemService(Context.NOTIFICATION_SERVICE);
            if ((StartedBroadcastEventStreaming.class == c) && notificationManager != null) {
                notificationManager.cancel(MessageNotificationBuilder.NOTIFICATION_ID);
            }
        }catch(Exception e){
            e.printStackTrace();
        }*/
    }

    //public void fireNotification(INewUpdatedContent content) {
        /*try {
            if (content == null)
                return;
            if (content.isHasBeenUiProcessed() || !content.notificationRequired()) {
                // If the object doesn't require any process by the UI or if it has been tagged as already
                // processed by the UI and has arrived here like by accident then we will do NOTHING
                // and simply return.
                return;
            }

            if (builders.containsKey(content.getClass())) {
                builders.get(content.getClass()).fireNotification(content);
            }
        }catch(Exception e){
            e.printStackTrace();
        }*/
   // }
}
