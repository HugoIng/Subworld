package com.deepred.subworld.engine;

import com.deepred.subworld.model.Rival;
import com.firebase.geofire.GeoLocation;

import java.util.HashMap;
import java.util.Set;

/**
 *
 */
public class RivalsMap {

    private HashMap<String, Rival> rivals;

    RivalsMap() {
        rivals = new HashMap();
    }

    public void put(String _uid, GeoLocation _loc, boolean visibility) {
        rivals.put(_uid, new Rival(_loc, visibility));
    }

    public Rival remove(String _uid) {
        Rival obj = rivals.get(_uid);
        rivals.remove(_uid);
        return obj;
    }

    public int countVisibleUsersInRange() {
        int count = 0;

        for (Rival r : rivals.values()) {
            if (r.isVisible())
                count++;
        }

        return count;
    }

    public boolean isVisible(String uid) {
        boolean ret = false;
        if (rivals.size() > 0) {
            Rival u = rivals.get(uid);
            if(u != null) {
                ret = u.isVisible();
            }
        }
        return ret;
    }

    public Set<String> getKeys() {
        return rivals.keySet();
    }

    public Rival get(String _uid) {
        return rivals.get(_uid);
    }
}
