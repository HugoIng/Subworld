package com.deepred.subworld.model;

import java.util.Date;

/**
 * Created by aplicaty on 25/02/16.
 */
public class Theft {
    private String thief;
    private String victim;
    private Date start;

    public Theft() {
        init();
    }

    public Theft(String thief, String victim, Date start) {
        this.thief = thief;
        this.victim = victim;
        this.start = start;
        init();
    }

    private void init() {
        start = new Date();
    }

    public String getThief() {
        return thief;
    }

    public void setThief(String thief) {
        this.thief = thief;
    }

    public String getVictim() {
        return victim;
    }

    public void setVictim(String victim) {
        this.victim = victim;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }
}
