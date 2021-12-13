package bassamalim.hidaya.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import bassamalim.hidaya.other.Constants;

public class Compass implements SensorEventListener {

    public interface CompassListener {
        void onNewAzimuth(float azimuth);
        void calibration(int accuracy);
    }

    private CompassListener listener;
    private final SensorManager sensorManager;
    private final Sensor mSensor;
    private final Sensor aSensor;
    private final float[] aData = new float[3];
    private final float[] mData = new float[3];
    private final float[] myR = new float[9];
    private final float[] myI = new float[9];
    private float azimuthFix;

    public Compass(Context gContext) {
        sensorManager = (SensorManager) gContext.getSystemService(Context.SENSOR_SERVICE);
        aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void start(Context context) {
        sensorManager.registerListener(this, aSensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_GAME);

        PackageManager manager = context.getPackageManager();
        boolean haveAS = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        boolean haveCS = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
        if (!haveAS || !haveCS) {
            sensorManager.unregisterListener(this, aSensor);
            sensorManager.unregisterListener(this, mSensor);
            Toast.makeText(context, "لايوجد حساسات كافية لحساب القبلة", Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "Device don't have enough sensors");
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            float alpha = 0.97f;
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                aData[0] = alpha * aData[0] + (1 - alpha) * event.values[0];
                aData[1] = alpha * aData[1] + (1 - alpha) * event.values[1];
                aData[2] = alpha * aData[2] + (1 - alpha) * event.values[2];
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mData[0] = alpha * mData[0] + (1 - alpha) * event.values[0];
                mData[1] = alpha * mData[1] + (1 - alpha) * event.values[1];
                mData[2] = alpha * mData[2] + (1 - alpha) * event.values[2];
            }

            boolean success = SensorManager.getRotationMatrix(myR, myI, aData, mData);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(myR, orientation);

                float azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                azimuth = (azimuth + azimuthFix + 360) % 360;

                if (listener != null) {
                    listener.onNewAzimuth(azimuth);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        listener.calibration(accuracy);
    }

    public void setListener(CompassListener l) {
        listener = l;
    }

    // used for something i don't know, thus don't need
    /*public void setAzimuthFix(float fix) {
        Log.i(Constants.TAG, "in qibla master setAzimuthFix");
        azimuthFix = fix;
    }

    public void resetAzimuthFix() {
        setAzimuthFix(0);
    }*/

}
