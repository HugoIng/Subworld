package com.deepred.subworld.engine;

import com.firebase.geofire.GeoLocation;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Hugo on 4/6/2016.
 */
public class UsersMap {

    class UserInRange {
        private GeoLocation loc;
        private boolean isVisible;

        UserInRange(GeoLocation _loc, boolean _isVisible) {
            loc = _loc;
            isVisible = _isVisible;
        }

        public boolean isVisible() {
            return isVisible;
        }

        public void setVisible(boolean visible) {
            isVisible = visible;
        }

        public GeoLocation getLocation() {
            return loc;
        }

        public void setLocation(GeoLocation _loc) {
            loc = _loc;
        }
    }


    private HashMap<String, UserInRange> users;

    UsersMap() {
        users = new HashMap<String, UserInRange>();
    }

    public void put(String _uid, GeoLocation _loc, boolean visibility) {
        users.put(_uid, new UserInRange(_loc, visibility));
    }

    public UserInRange remove (String _uid) {
        UserInRange obj = users.get(_uid);
        users.remove(_uid);
        return obj;
    }

    public int countVisibleUsersInRange() {
        int count = 0;



        return count;
    }

    public boolean isVisible(String uid) {
        boolean ret = false;
        if(users.size() > 0) {
            UserInRange u = users.get(uid);
            if(u != null) {
                ret = u.isVisible();
            }
        }
        return ret;
    }

    public Set<String> getKeys() {
        return users.keySet();
    }

    public UserInRange get(String _uid) {
        return users.get(_uid);
    }
}
