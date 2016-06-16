package com.deepred.subworld.model;

import com.deepred.subworld.ICommon;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aplicaty on 25/02/16.
 */
public class User {
    private String uid;
    private String email;
    private String name;
    private int chrType;
    private Map<String, Treasure> backpack; // Represent treasures carried by the user
    private Map<String, Treasure> hidden; // Represent treasures hidden and actually owned by the user
    private Map<String, Treasure> lost; // Represent treasures lost by the user. This cannot be recovered by the same user
    private Map<String, Treasure> failedRetrievals; // Represent failed attempts to get treasures. These are eliminated in 3 hours and the user can retry.
    private Map<String, Treasure> thefts;
    private Skills skills;


    public User() {
        init();
    }

    public User(String uid) {
        this.uid = uid;
        this.name = "";
        this.chrType = ICommon.CHRS_NOT_SET;
        init();
    }

    public User(String uid, String name, String email, int chrType) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.chrType = chrType;
        init();
    }

    private void init() {
        skills = new Skills();
        skills.setSkills(ICommon.skillsTable[chrType]);

        backpack = new HashMap<>();
        hidden = new HashMap<>();
        lost = new HashMap<>();
        failedRetrievals = new HashMap<>();
        thefts = new HashMap<>();
    }

    public static String createTreasureId(String uid, Date fecha) {
        return uid + "_" + fecha.getTime();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getChrType() {
        return chrType;
    }

    public void setChrType(int chr_type) {
        this.chrType = chr_type;
        //skills.setSkills(ICommon.skillsTable[chr_type]);
    }

    public Map<String, Treasure> getBackpack() {
        return backpack;
    }

    public void setBackpack(Map<String, Treasure> backpack) {
        this.backpack = backpack;
    }

    public Map<String, Treasure> getHidden() {
        return hidden;
    }

    public void setHidden(Map<String, Treasure> hidden) {
        this.hidden = hidden;
    }

    public Map<String, Treasure> getLost() {
        return lost;
    }

    public void setLost(HashMap<String, Treasure> lost) {
        this.lost = lost;
    }

    public Map<String, Treasure> getFailedRetrievals() {
        return failedRetrievals;
    }

    public void setFailedRetrievals(Map<String, Treasure> failedRetrievals) {
        this.failedRetrievals = failedRetrievals;
    }

    public Map<String, Treasure> getThefts() {
        return thefts;
    }

    public void setThefts(HashMap<String, Treasure> thefts) {
        this.thefts = thefts;
    }

    public Skills getSkills() {
        return skills;
    }

    public void setSkills(Skills skills) {
        this.skills = skills;
    }
}
