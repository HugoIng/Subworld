package com.deepred.subworld.model;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;

/**
 * Created by Manuel.Gutierrez on 30/06/2016.
 */
public class MapMarker {
    private MarkerOptions mo;
    private int type;
    private String id;

    public MapMarker(MarkerOptions mo, int type, String id) {
        this.mo = mo;
        this.type = type;
        this.id = id;
    }

    public MarkerOptions getMo() {
        return mo;
    }

    public void setMo(MarkerOptions mo) {
        this.mo = mo;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
