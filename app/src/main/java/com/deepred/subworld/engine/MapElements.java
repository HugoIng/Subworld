package com.deepred.subworld.engine;

import com.deepred.subworld.model.MapElement;
import com.deepred.subworld.model.MapRival;
import com.firebase.geofire.GeoLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MapElements {

    private Map<String, MapElement> elems;

    MapElements() {
        elems = new HashMap<>();
    }

    public void put(String _uid, GeoLocation _loc, boolean visibility) {
        elems.put(_uid, new MapRival(_loc, visibility));
    }

    public MapElement remove(String _uid) {
        MapElement obj = elems.get(_uid);
        elems.remove(_uid);
        return obj;
    }

    public int countVisibleUsersInRange() {
        int count = 0;

        for (MapElement r : elems.values()) {
            if (r.isVisible())
                count++;
        }

        return count;
    }

    public boolean isVisible(String uid) {
        boolean ret = false;
        if (elems.size() > 0) {
            MapElement u = elems.get(uid);
            if(u != null) {
                ret = u.isVisible();
            }
        }
        return ret;
    }

    public Set<String> getKeys() {
        return elems.keySet();
    }

    public MapElement get(String _uid) {
        return elems.get(_uid);
    }
}
