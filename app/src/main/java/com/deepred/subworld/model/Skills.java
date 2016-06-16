package com.deepred.subworld.model;

import com.deepred.subworld.ICommon;

/**
 * Created by aplicaty on 25/02/16.
 */
public class Skills {
    private Skill finding;
    private Skill hiding;
    private Skill opening;

    private Skill watching;
    private Skill stealth;

    private Skill stealing;
    private Skill defense;

    public Skills() {
    }

    public Skills(Skill finding, Skill hiding, Skill opening, Skill watching, Skill stealth, Skill stealing, Skill defense) {
        this.finding = finding;
        this.hiding = hiding;
        this.opening = opening;
        this.watching = watching;
        this.stealth = stealth;
        this.stealing = stealing;
        this.defense = defense;
    }

    public void setSkills(int[][] s) {
        if(s.length == 7) {
            finding = new Skill(s[0][0], ICommon.EvolutionFactorValues[s[0][1]]);
            hiding = new Skill(s[1][0], ICommon.EvolutionFactorValues[s[1][1]]);
            opening = new Skill(s[2][0], ICommon.EvolutionFactorValues[s[2][1]]);
            watching = new Skill(s[3][0], ICommon.EvolutionFactorValues[s[3][1]]);
            stealth = new Skill(s[4][0], ICommon.EvolutionFactorValues[s[4][1]]);
            stealing = new Skill(s[5][0], ICommon.EvolutionFactorValues[s[5][1]]);
            defense = new Skill(s[6][0], ICommon.EvolutionFactorValues[s[6][1]]);
        }
    }

    public Skill getFinding() {
        return finding;
    }

    public void setFinding(Skill finding) {
        this.finding = finding;
    }

    public Skill getHiding() {
        return hiding;
    }

    public void setHiding(Skill hiding) {
        this.hiding = hiding;
    }

    public Skill getOpening() {
        return opening;
    }

    public void setOpening(Skill opening) {
        this.opening = opening;
    }

    public Skill getWatching() {
        return watching;
    }

    public void setWatching(Skill watching) {
        this.watching = watching;
    }

    public Skill getStealth() {
        return stealth;
    }

    public void setStealth(Skill stealth) {
        this.stealth = stealth;
    }

    public Skill getStealing() {
        return stealing;
    }

    public void setStealing(Skill stealing) {
        this.stealing = stealing;
    }

    public Skill getDefense() {
        return defense;
    }

    public void setDefense(Skill defense) {
        this.defense = defense;
    }
}
