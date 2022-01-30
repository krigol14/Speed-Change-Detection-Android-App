package com.example.proairetikh_1;

public class Data {
    public String timestamp;
    double speed, longitude, latitude, acceleration_deceleration;

    public Data(){}

    // constructor for Acceleration & Deceleration in firebase
    public Data(double acceleration_deceleration, double longitude, double latitude, double speed) {
        this.acceleration_deceleration = acceleration_deceleration;
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
    }

    // constructor for Detailed Data in firebase
    public Data(String timestamp, double speed, double longitude, double latitude) {
        this.timestamp = timestamp;
        this.speed = speed;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAcceleration_deceleration() {
        return acceleration_deceleration;
    }

    public void setAcceleration_deceleration(double acceleration_deceleration) {
        this.acceleration_deceleration = acceleration_deceleration;
    }
}