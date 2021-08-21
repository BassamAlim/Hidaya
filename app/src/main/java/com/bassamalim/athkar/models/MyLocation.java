package com.bassamalim.athkar.models;

import android.location.Location;
import android.os.Build;
import java.io.Serializable;

public class MyLocation implements Serializable {

    private float accuracy;
    private long time;
    private double altitude;
    private float bearing;
    private float bearingAccuracyDegrees;
    private long elapsedRealtimeNanos;
    private double elapsedRealtimeUncertaintyNanos;
    //private Bundle extras;
    private double latitude;
    private double longitude;
    private String provider;
    private float speed;
    private float speedAccuracyMetersPerSecond;
    private float verticalAccuracyMeters;


    public MyLocation(String provider) {
        this.provider = provider;
    }

    public MyLocation(Location loc) {
        accuracy = loc.getAccuracy();
        time = loc.getTime();
        altitude = loc.getAltitude();
        bearing = loc.getBearing();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bearingAccuracyDegrees = loc.getBearingAccuracyDegrees();
            speedAccuracyMetersPerSecond = loc.getSpeedAccuracyMetersPerSecond();
            verticalAccuracyMeters = loc.getVerticalAccuracyMeters();
        }
        elapsedRealtimeNanos = loc.getElapsedRealtimeNanos();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            elapsedRealtimeUncertaintyNanos = loc.getElapsedRealtimeUncertaintyNanos();
        //extras = loc.getExtras();
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        provider = loc.getProvider();
        speed = loc.getSpeed();

    }


    public float getAccuracy() {
        return accuracy;
    }

    public long getTime() {
        return time;
    }

    public double getAltitude() {
        return altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public float getBearingAccuracyDegrees() {
        return bearingAccuracyDegrees;
    }

    public long getElapsedRealtimeNanos() {
        return elapsedRealtimeNanos;
    }

    public double getElapsedRealtimeUncertaintyNanos() {
        return elapsedRealtimeUncertaintyNanos;
    }

    /*public Bundle getExtras() {
        return extras;
    }*/

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getProvider() {
        return provider;
    }

    public float getSpeed() {
        return speed;
    }

    public float getSpeedAccuracyMetersPerSecond() {
        return speedAccuracyMetersPerSecond;
    }

    public float getVerticalAccuracyMeters() {
        return verticalAccuracyMeters;
    }

}
