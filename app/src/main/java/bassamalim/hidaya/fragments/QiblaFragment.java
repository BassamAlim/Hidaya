package bassamalim.hidaya.fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.MainActivity;
import bassamalim.hidaya.databinding.FragmentQiblaBinding;
import bassamalim.hidaya.helpers.Compass;
import bassamalim.hidaya.popups.CalibrationPopup;

public class QiblaFragment extends Fragment {

    private final double KAABA_LAT = 21.4224779;
    private final double KAABA_LAT_IN_RAD  = Math.toRadians(KAABA_LAT);
    private final double KAABA_LNG = 39.8251832;
    private FragmentQiblaBinding binding;
    private boolean located = true;
    private Compass compass;
    private Location location;
    private float currentAzimuth;
    private double distance;
    private float bearing;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MainActivity.located) {
            location = MainActivity.location;

            distance = getDistance();
            bearing = calculateBearing();

            setupCompass();

            if (compass != null)
                compass.start(requireContext());
        }
        else
            located = false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQiblaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (!located)
            binding.distanceText.setText("يجب السماح بالوصول للموقع لحساب اتجاه القبلة");
        else {
            String distanceText = "المسافة الى الكعبة: " +
                    translateNumbers(String.valueOf(distance)) + " كم";
            binding.distanceText.setText(distanceText);

            binding.accuracyIndicator.setBackgroundColor(Color.TRANSPARENT);
        }

        inflater.inflate(R.layout.fragment_qibla, container, false);
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (compass != null)
            compass.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (located && compass != null)
            compass.start(requireContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (compass != null)
            compass.stop();
    }

    private void setupCompass() {
        compass = new Compass(requireContext());

        Compass.CompassListener listener = new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                adjust(azimuth);
                adjustNorthDial(azimuth);
            }

            @Override
            public void calibration(int accuracy) {
                updateAccuracy(accuracy);
            }
        };

        compass.setListener(listener);
    }

    private void adjust(float azimuth) {
        float target = bearing - currentAzimuth;

        Animation rotate = new RotateAnimation(target, -azimuth, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.565f);
        rotate.setDuration(500);
        rotate.setRepeatCount(0);
        rotate.setFillAfter(true);
        binding.qiblaPointer.startAnimation(rotate);

        currentAzimuth = (azimuth);

        if (target > -2 && target < 2)
            binding.bingo.setVisibility(View.VISIBLE);
        else
            binding.bingo.setVisibility(View.INVISIBLE);
    }

    // maybe points north
    public void adjustNorthDial(float azimuth) {
        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = (azimuth);
        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        binding.compass.startAnimation(an);
    }

    public double getDistance() {
        final double EARTH_RADIUS = 6371;
        double dLon = Math.toRadians(Math.abs(location.getLatitude() - KAABA_LAT));
        double dLat = Math.toRadians(Math.abs(location.getLongitude() - KAABA_LNG));
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(
                location.getLatitude())) * Math.cos(Math.toRadians(KAABA_LAT))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        distance = EARTH_RADIUS * c;
        distance = (int) (distance * 10) / 10.0;

        return distance;
    }

    private float calculateBearing() {
        float result;
        double myLatRad = Math.toRadians(location.getLatitude());
        double lngDiff = Math.toRadians(KAABA_LNG - location.getLongitude());
        double y = Math.sin(lngDiff) * Math.cos(KAABA_LAT_IN_RAD);
        double x = Math.cos(myLatRad) * Math.sin(KAABA_LAT_IN_RAD) - Math.sin(myLatRad)
                * Math.cos(KAABA_LAT_IN_RAD) * Math.cos(lngDiff);
        result = (float) ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360);

        return result;
    }

    private void updateAccuracy(int accuracy) {
        switch (accuracy) {
            case 3:
                binding.accuracyText.setText(R.string.high_accuracy_text);
                binding.accuracyIndicator.setImageDrawable(AppCompatResources
                        .getDrawable(requireContext(), R.drawable.green_dot));
                binding.accuracyIndicator.setOnClickListener(null);
                break;
            case 2:
                binding.accuracyText.setText(R.string.medium_accuracy_text);
                binding.accuracyIndicator.setImageDrawable(AppCompatResources
                        .getDrawable(requireContext(), R.drawable.yellow_dot));
                binding.accuracyIndicator.setOnClickListener(null);
                break;
            case 0:
            case 1:
                binding.accuracyText.setText(R.string.low_accuracy_text);
                binding.accuracyIndicator.setImageDrawable(AppCompatResources.getDrawable(
                        requireContext(), R.drawable.ic_warning));
                binding.accuracyIndicator.setOnClickListener(v -> new CalibrationPopup(getContext())
                        .show(requireActivity().getSupportFragmentManager(), CalibrationPopup.TAG));
                break;
        }
    }

    private String translateNumbers(String english) {
        String result;
        HashMap<Character, Character> map = new HashMap<>();
        map.put('0', '٠');
        map.put('1', '١');
        map.put('2', '٢');
        map.put('3', '٣');
        map.put('4', '٤');
        map.put('5', '٥');
        map.put('6', '٦');
        map.put('7', '٧');
        map.put('8', '٨');
        map.put('9', '٩');
        map.put('A', 'ص');
        map.put('P', 'م');

        if (english.charAt(0) == '0') {
            english = english.replaceFirst("0", "");
            if (english.charAt(0) == '0')
                english = english.replaceFirst("0:", "");
        }
        english = english.replaceAll(":0", ":");

        StringBuilder temp = new StringBuilder();
        for (int j = 0; j < english.length(); j++) {
            char t = english.charAt(j);
            if (map.containsKey(t))
                t = map.get(t);
            temp.append(t);
        }
        result = temp.toString();

        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}