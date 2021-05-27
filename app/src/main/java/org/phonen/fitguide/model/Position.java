package org.phonen.fitguide.model;

public class Position {
    boolean moving;
    double latitude;
    double longitude;

    public Position() {
        moving = false;
        latitude = 0;
        longitude = 0;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
