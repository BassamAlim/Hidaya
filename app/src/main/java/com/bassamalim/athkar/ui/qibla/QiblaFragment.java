package com.bassamalim.athkar.ui.qibla;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.FragmentQiblaBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.os.Vibrator;

public class QiblaFragment extends Fragment implements SensorEventListener {

    private QiblaViewModel qiblaViewModel;
    private FragmentQiblaBinding binding;
    private Sensor sensor;
    private SensorManager sensorManager;
    private FusedLocationProviderClient fusedLocationClient;
    private final Location kaaba = new Location(LocationManager.GPS_PROVIDER);
    private final double kaabaLongitude = 39.8251832;
    private final double kaabaLatitude = 21.4224779;
    private Location location = MainActivity.userLocation;
    private float bearing;
    private float currentDegree = 0f;
    private double distance = 0;
    private final double earthRadius = 6371;
    Vibrator vibrator;
    VibrationEffect vibrationEffect;


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

        kaaba.setLatitude(kaabaLatitude);
        kaaba.setLongitude(kaabaLongitude);

        if (sensor != null)
            // for the system's orientation sensor registered listeners
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);//SensorManager.SENSOR_DELAY_Fastest
        else
            Toast.makeText(getContext(),"Not Supported", Toast.LENGTH_SHORT).show();

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        qiblaViewModel = new ViewModelProvider(this).get(QiblaViewModel.class);

        binding = FragmentQiblaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        inflater.inflate(R.layout.fragment_qibla, container, false);
        return root;
    }

    public double degToRad(double degree) {
        return degree * (Math.PI / 180);
    }

    public void getDistance() {
        double dLon = degToRad(Math.abs(location.getLatitude() - kaabaLatitude));
        double dLat = degToRad(Math.abs(location.getLongitude() - kaabaLongitude));
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(degToRad(location.getLatitude()))
                * Math.cos(degToRad(kaabaLatitude)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        distance = earthRadius * c;
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getDistance();

            // get the angle around the z-axis rotated
            float degree = Math.round(event.values[0]);
            float head = Math.round(event.values[0]);

            bearing = location.bearingTo(kaaba);

            GeomagneticField geoField = new GeomagneticField(Double.valueOf(location.getLatitude()).floatValue(),
                    Double.valueOf(location.getLongitude()).floatValue(),
                    Double.valueOf(location.getAltitude()).floatValue(), System.currentTimeMillis());
            head -= geoField.getDeclination(); // converts magnetic north into true north

            if (bearing < 0) {
                bearing = bearing + 360;
                //bearTo = -100 + 360  = 260;
            }

            //This is where we choose to point it
            float direction = bearing - head;

            // If the direction is smaller than 0, add 360 to get the rotation clockwise.
            if (direction < 0)
                direction = direction + 360;

            RotateAnimation raQibla = new RotateAnimation(currentDegree, direction,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            raQibla.setDuration(210);
            raQibla.setFillAfter(true);

            binding.qiblaView.startAnimation(raQibla);

            currentDegree = direction;

            String text = "المسافة الى الكعبة: " + distance + " كم";
            binding.distanceText.setText(text);


            if (degree == 111 || degree == 112) {
                /*vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                Toast.makeText(getContext(), "بس بس كدا", Toast.LENGTH_SHORT).show();*/
                binding.bingo.setVisibility(View.VISIBLE);
            }
            if (degree < 111 || degree > 112)
                binding.bingo.setVisibility(View.INVISIBLE);

        }
        else {
            binding.distanceText.setText("لازم تسوي سماح للموقع عشان يشتغل");
            MainActivity.requestPermissions();
        }

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