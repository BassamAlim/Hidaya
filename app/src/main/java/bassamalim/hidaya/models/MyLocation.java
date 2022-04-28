package bassamalim.hidaya.models;

import android.location.Location;
import android.os.Build;

import java.io.Serializable;

public class MyLocation implements Serializable {

    private final float accuracy;
    private final long time;
    private final double altitude;
    private final float bearing;
    private float bearingAccuracyDegrees;
    private final long elapsedRealtimeNanos;
    private double elapsedRealtimeUncertaintyNanos;
    private final double latitude;
    private final double longitude;
    private final String provider;
    private final float speed;
    private float speedAccuracyMetersPerSecond;
    private float verticalAccuracyMeters;

    public MyLocation(Location loc) {
        accuracy = loc.getAccuracy();
        time = loc.getTime();
        altitude = loc.getAltitude();
        bearing = loc.getBearing();
        elapsedRealtimeNanos = loc.getElapsedRealtimeNanos();
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        provider = loc.getProvider();
        speed = loc.getSpeed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bearingAccuracyDegrees = loc.getBearingAccuracyDegrees();
            speedAccuracyMetersPerSecond = loc.getSpeedAccuracyMetersPerSecond();
            verticalAccuracyMeters = loc.getVerticalAccuracyMeters();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                elapsedRealtimeUncertaintyNanos = loc.getElapsedRealtimeUncertaintyNanos();
        }
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

    public static Location toLocation(MyLocation myLoc) {
        Location loc = new Location("provider");

        loc.setAccuracy(myLoc.getAccuracy());
        loc.setTime(myLoc.getTime());
        loc.setAltitude(myLoc.getAltitude());
        loc.setBearing(myLoc.getBearing());
        loc.setElapsedRealtimeNanos(myLoc.getElapsedRealtimeNanos());
        loc.setLatitude(myLoc.getLatitude());
        loc.setLongitude(myLoc.getLongitude());
        loc.setProvider(myLoc.getProvider());
        loc.setSpeed(myLoc.getSpeed());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loc.setBearingAccuracyDegrees(myLoc.getBearingAccuracyDegrees());
            loc.setSpeedAccuracyMetersPerSecond(myLoc.getSpeedAccuracyMetersPerSecond());
            loc.setVerticalAccuracyMeters(myLoc.getVerticalAccuracyMeters());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                loc.setElapsedRealtimeUncertaintyNanos(loc.getElapsedRealtimeUncertaintyNanos());
        }

        return loc;
    }

}
