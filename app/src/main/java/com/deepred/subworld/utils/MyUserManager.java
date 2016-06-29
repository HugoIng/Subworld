package com.deepred.subworld.utils;

import com.deepred.subworld.model.User;

import java.util.ArrayList;

/**
 *
 */
public class MyUserManager {
    private static Object lock = new Object();
    private static volatile MyUserManager instance = null;
    private ArrayList<ICallbacks.IUserCallbacks> activities;
    private User user;

    private MyUserManager() {
        user = null;
        activities = new ArrayList<>();
    }

    public static MyUserManager getInstance() {
        MyUserManager localInstance = instance;
        if(localInstance == null) {
            synchronized (lock) {
                localInstance = instance;
                if(localInstance == null) {
                    instance = localInstance = new MyUserManager();
                }
            }
        }
        return localInstance;
    }

    public void register4UserNotifications(ICallbacks.IUserCallbacks act) {
        activities.add(act);
    }

    public void unregister4UserNotifications(ICallbacks.IUserCallbacks act) {
        if(activities.contains(act))
            activities.remove(act);
    }

    public User getUser() {
        return user!=null?user : new User();
    }

    public User getUser(String uid) {
        return user!=null?user : new User(uid);
    }

    public void updateUser(User u) {
        user = u;

        for (ICallbacks.IUserCallbacks act : activities) {
            act.onUserChange(user);
        }
    }
}
