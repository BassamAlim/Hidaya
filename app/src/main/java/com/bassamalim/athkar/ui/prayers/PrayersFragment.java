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
import java.util.Calendar;
import java.util.Date;

public class PrayersFragment extends Fragment {

    private PrayersViewModel prayersViewModel;
    private PrayersFragmentBinding binding;
    private Location location;
    private String[] times;
    private Calendar calendar;
    private Date now;
    private final int timeZone = 3;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        prayersViewModel = new ViewModelProvider(this).get(PrayersViewModel.class);

        binding = PrayersFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        location = MainActivity.location;

        return root;
        //return inflater.inflate(R.layout.prayers_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.checkPermissions()) {
            times = getTimes();

            String fajrText = "الفجر: " + times[0];
            String shorouqText = "الشروق: " + times[1];
            String dhuhrText = "الظهر: " + times[2];
            String asrText = "العصر: " + times[3];
            String maghribText = "المغرب: " + times[5];
            String ishaaText = "العشاء: " + times[6];

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

    public String[] getTimes() {
        now = new Date();

        calendar = Calendar.getInstance();
        calendar.setTime(now);

        return new PrayTimes().getPayerTimesArray(calendar, location.getLatitude(), location.getLongitude(), timeZone);
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}