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
import java.util.HashMap;
import java.util.Map;

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

            String[] times = translateNumbers(prayerTimes);

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

    public static String[] translateNumbers(ArrayList<String> english) {
        String[] result = english.toArray(new String[english.size()]);

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

        for (int i = 0; i < english.size(); i++) {
            if (english.get(i).charAt(0) == '0')
                english.set(i, english.get(i).replaceFirst("0", ""));
        }

        for (int i = 0; i < english.size(); i++) {
            StringBuilder temp = new StringBuilder();
            for (int j = 0; j < english.get(i).length(); j++) {
                char t = english.get(i).charAt(j);
                if (map.containsKey(t))
                    t = map.get(t);
                temp.append(t);
            }
            result[i] = temp.toString();
        }

        return result;
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}