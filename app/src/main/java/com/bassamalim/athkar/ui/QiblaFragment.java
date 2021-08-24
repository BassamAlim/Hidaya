package com.bassamalim.athkar.ui;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.FragmentQiblaBinding;

public class QiblaFragment extends Fragment implements SensorEventListener {

    private FragmentQiblaBinding binding;
    private Sensor sensor;
    private SensorManager sensorManager;
    private final Location kaaba = new Location(LocationManager.GPS_PROVIDER);
    private Location location;
    private float currentDegree = 0f;
    private double distance = 0;
    GeomagneticField geoField;
    RotateAnimation raQibla;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        location = MainActivity.location;

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        kaaba.setLatitude(Constants.KAABA_LATITUDE);
        kaaba.setLongitude(Constants.KAABA_LONGITUDE);

        if (sensor != null)
            // for the system's orientation sensor registered listeners
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);//SensorManager.SENSOR_DELAY_Fastest
        else
            Toast.makeText(getContext(),"Not Supported", Toast.LENGTH_SHORT).show();

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQiblaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getDistance();

        //orientation = SensorManager.getOrientation()

        inflater.inflate(R.layout.fragment_qibla, container, false);
        return root;
    }

    public double degToRad(double degree) {
        return degree * (Math.PI / 180);
    }

    public void getDistance() {
        double dLon = degToRad(Math.abs(location.getLatitude() - Constants.KAABA_LATITUDE));
        double dLat = degToRad(Math.abs(location.getLongitude() - Constants.KAABA_LONGITUDE));
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(degToRad(location.getLatitude()))
                * Math.cos(degToRad(Constants.KAABA_LATITUDE)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        distance = Constants.EARTH_RADIUS * c;
        distance = (int) (distance * 10) / 10.0;
    }

    @Override
    public void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        float head = Math.round(event.values[0]);

        float bearing = location.bearingTo(kaaba);

        geoField = new GeomagneticField(Double.valueOf(location.getLatitude()).floatValue(),
                Double.valueOf(location.getLongitude()).floatValue(),
                Double.valueOf(location.getAltitude()).floatValue(), System.currentTimeMillis());
        head -= geoField.getDeclination(); // converts magnetic north into true north

        if (bearing < 0)
            bearing = bearing + 360;

        //This is where we choose to point it
        float direction = bearing - head;

        // If the direction is smaller than 0, add 360 to get the rotation clockwise.
        if (direction < 0)
            direction = direction + 360;

        raQibla = new RotateAnimation(currentDegree, direction,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        raQibla.setDuration(210);
        raQibla.setFillAfter(true);

        binding.qiblaView.startAnimation(raQibla);

        currentDegree = direction;

        String text = "المسافة الى الكعبة: " + distance + " كم";
        binding.distanceText.setText(text);

        if (direction > 358 && direction < 360 || direction > 0 && direction < 2)
            binding.bingo.setVisibility(View.VISIBLE);
        else
            binding.bingo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sensorManager.unregisterListener(this);
        binding = null;
    }
}