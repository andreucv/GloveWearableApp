package com.example.andreucortes.glovebluetooth;

import java.io.Serializable;

public class Gesture implements Serializable {
    String title;
    String commandBLE;
    int actionIndex;
    String telf;

    public Gesture() {
        this.title = "New Title";
        this.commandBLE = null;
        this.actionIndex = -1;
    }

    public Gesture(String title, String commandBLE) {
        this.title = title;
        this.commandBLE = commandBLE;
    }

    public Gesture(String title, String commandBLE, int actionIndex) {
        this.title = title;
        this.commandBLE = commandBLE;
        this.actionIndex = actionIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCommandBLE() {
        return commandBLE;
    }

    public void setCommandBLE(String commandBLE) {
        this.commandBLE = commandBLE;
    }

    public String[] getAction() {
        String[] strings = {actionIndex+"", telf};
        return strings;
    }

    public void setAction(int action) {
        this.actionIndex = action;
    }

    public boolean hasAction() {
        return actionIndex != -1;
    }

    public String getTelf() {
        return telf;
    }

    public void setTelf(String telf) {
        this.telf = telf;
    }
}

