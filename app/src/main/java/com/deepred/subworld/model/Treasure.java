package com.deepred.subworld.model;

import com.deepred.subworld.ICommon;

import java.util.Date;
import java.util.Random;

/**
 *
 */
public class Treasure {
    private String uid;
    private int type;
    private Date created;
    private Date obtained;
    private Date hidden;
    private String owner;
    private int value;

    public Treasure() {
        /*owner = "";
        type = 0;
        value = 0;
        location = new Location("1,1");
        init();*/
    }

    public Treasure(int _type, String _owner, int _value) {
        type = _type;
        owner = _owner;
        value = _value;
        init();
    }

    /*
        Crea un nuevo tesoro aleatorio para un nuevo usuario
     */
    public Treasure(String _owner) {
        owner = _owner;
        created = new Date();
        obtained = created;
        hidden = null;

        // Select random type
        int min = 0;
        int max = 7;

        Random r = new Random();
        type = r.nextInt(max - min + 1) + min;
        value = ICommon.defaultTreasureValues[type];
    }

    private void init() {
        created = obtained = new Date();
        hidden = null;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getObtained() {
        return obtained;
    }

    public void setObtained(Date obtained) {
        this.obtained = obtained;
    }

    public Date getHidden() {
        return hidden;
    }

    public void setHidden(Date hidden) {
        this.hidden = hidden;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
