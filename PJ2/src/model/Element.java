package model;

import javafx.scene.image.Image;

import java.io.Serializable;

public abstract class Element implements Serializable {
    protected volatile boolean running = true;
    protected volatile boolean paused = false;
    protected final Object pauseLock = new Object();

    public void paintElement(int x, int y, boolean reset) {
    }

    public void setPicture(Image image) {
    }

    public boolean isCitizen() {
        return false;
    }

    public boolean isAmbulance() {
        return false;
    }

    public boolean isHospital() {
        return false;
    }

    public boolean isHospitalOrHouse() {
        return false;
    }

    public boolean isCheckPoint() {
        return false;
    }
}
