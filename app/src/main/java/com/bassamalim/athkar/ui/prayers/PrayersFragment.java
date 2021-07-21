package com.bassamalim.athkar.ui.prayers;

import androidx.lifecycle.ViewModelProvider;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.databinding.PrayersFragmentBinding;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PrayersFragment extends Fragment {

    private PrayersViewModel prayersViewModel;
    private PrayersFragmentBinding binding;
    private PrayTimes prayTimes;
    private Calendar calendar;
    private Date now;
    private ArrayList<String> prayerTimes;
    private final int timeZone = 3;
    private Location location;

    /*public static PrayersFragment newInstance() {
        return new PrayersFragment();
    }*/

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        prayersViewModel = new ViewModelProvider(this).get(PrayersViewModel.class);

        binding = PrayersFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        location = MainActivity.userLocation;

        now = new Date();

        calendar = Calendar.getInstance();
        calendar.setTime(now);

        return root;
        //return inflater.inflate(R.layout.prayers_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.checkPermissions()) {

            prayTimes = new PrayTimes();
            prayerTimes = prayTimes.getPrayerTimes(calendar, location.getLatitude(),
                    location.getLongitude(), timeZone);

            String fajrText = "الفجر: " + prayerTimes.get(0);
            String shorouqText = "الشروق: " + prayerTimes.get(1);
            String dhuhrText = "الظهر: " + prayerTimes.get(2);
            String asrText = "العصر: " + prayerTimes.get(3);
            String maghribText = "المغرب: " + prayerTimes.get(5);
            String ishaaText = "العشاء: " + prayerTimes.get(6);

            binding.fajrView.setText(fajrText);
            binding.shorouqView.setText(shorouqText);
            binding.dhuhrView.setText(dhuhrText);
            binding.asrView.setText(asrText);
            binding.maghribView.setText(maghribText);
            binding.ishaaView.setText(ishaaText);
        }
        else {
            binding.asrView.setText("لازم تسوي سماح للموقع عشان يشتغل");
            MainActivity.requestPermissions();
        }
    }


    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}