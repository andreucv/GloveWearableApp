package com.example.andreucortes.glovebluetooth;

/**
 * Created by andreucortes on 31/07/15.
 */
public class Action {
    private String name;
    private Runnable r;

    public Action(String name, Runnable r) {
        this.name = name;
        this.r = r;
    }

    public Action(String name, Runnable r, String telf) {
        this.name = name;
        this.r = r;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Runnable getR() {
        return r;
    }

    public void setR(Runnable r) {
        this.r = r;
    }
}


