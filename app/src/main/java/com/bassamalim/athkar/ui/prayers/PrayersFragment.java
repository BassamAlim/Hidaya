package com.bassamalim.athkar.ui.prayers;

import androidx.lifecycle.ViewModelProvider;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bassamalim.athkar.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.DataSaver;
import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.databinding.PrayersFragmentBinding;
import com.bassamalim.athkar.receivers.NotificationReceiver;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class PrayersFragment extends Fragment {

    private PrayersViewModel prayersViewModel;
    private PrayersFragmentBinding binding;
    private Location location;
    public static ArrayList<String> times;
    public Calendar[] formattedTimes;
    public static String[] translatedTimes;
    public Gson gson;
    public static String json;
    public SharedPreferences myPrefs;
    public SharedPreferences.Editor editor;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        prayersViewModel = new ViewModelProvider(this).get(PrayersViewModel.class);

        binding = PrayersFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        location = MainActivity.location;

        times = getTimes();

        formattedTimes = formatTimes(times);

        //MyNotification notification = new MyNotification(formattedTimes);

        storeTimes(formatTimes(times));

        Alarms alarms = new Alarms(requireContext().getApplicationContext(), location, formattedTimes);

        //setAlarms(test());

        return root;
        //return inflater.inflate(R.layout.prayers_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        translatedTimes = translateNumbers(times);

        if (MainActivity.checkPermissions()) {

            String fajrText = "الفجر: " + translatedTimes[0];
            String shorouqText = "الشروق: " + translatedTimes[1];
            String dhuhrText = "الظهر: " + translatedTimes[2];
            String asrText = "العصر: " + translatedTimes[3];
            String maghribText = "المغرب: " + translatedTimes[5];
            String ishaaText = "العشاء: " + translatedTimes[6];

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

    public ArrayList<String> getTimes() {
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        return new PrayTimes().getPrayerTimes(calendar, location.getLatitude(), location.getLongitude(), Constants.TIME_ZONE);
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
        map.put('A', 'ص');
        map.put('P', 'م');

        for (int i = 0; i < english.size(); i++) {
            StringBuilder sb = new StringBuilder(english.get(i));
            sb.deleteCharAt(sb.length()-1);

            english.set(i, sb.toString());

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

    private Calendar[] formatTimes(ArrayList<String> givenTimes) {
        Calendar[] formattedTimes = new Calendar[givenTimes.size()];

        for (int i = 0; i < givenTimes.size(); i++) {
            char m = givenTimes.get(i).charAt(6);
            int hour = Integer.parseInt(givenTimes.get(i).substring(0, 2));
            if (m == 'P')
                hour += 12;

            formattedTimes[i] = Calendar.getInstance();
            formattedTimes[i].setTimeInMillis(System.currentTimeMillis());
            formattedTimes[i].set(Calendar.HOUR_OF_DAY, hour);
            formattedTimes[i].set(Calendar.MINUTE, Integer.parseInt(givenTimes.get(i).substring(3, 5)));
        }
        return formattedTimes;
    }

    public void storeTimes(Calendar[] givenTimes) {
        myPrefs = getContext().getApplicationContext().getSharedPreferences("times", Context.MODE_PRIVATE);
        //may produce null;
        editor = myPrefs.edit();

        DataSaver saver = new DataSaver();
        saver.times = givenTimes;

        gson = new Gson();

        json = gson.toJson(saver);

        editor.putString("times", json);
        editor.apply();
    }

    public Calendar[] test() {
        Calendar[] tester = new Calendar[7];

        tester[0] = Calendar.getInstance();
        tester[0].setTimeInMillis(System.currentTimeMillis());
        tester[0].set(Calendar.HOUR_OF_DAY, 5);
        tester[0].set(Calendar.MINUTE, 43);

        tester[1] = Calendar.getInstance();
        tester[1].setTimeInMillis(System.currentTimeMillis());
        tester[1].set(Calendar.HOUR_OF_DAY, 19);
        tester[1].set(Calendar.MINUTE, 35);

        tester[2] = Calendar.getInstance();
        tester[2].setTimeInMillis(System.currentTimeMillis());
        tester[2].set(Calendar.HOUR_OF_DAY, 1);
        tester[2].set(Calendar.MINUTE, 35);

        tester[3] = Calendar.getInstance();
        tester[3].setTimeInMillis(System.currentTimeMillis());
        tester[3].set(Calendar.HOUR_OF_DAY, 2);
        tester[3].set(Calendar.MINUTE, 35);

        tester[4] = Calendar.getInstance();
        tester[4].setTimeInMillis(System.currentTimeMillis());
        tester[4].set(Calendar.HOUR_OF_DAY, 18);
        tester[4].set(Calendar.MINUTE, 35);

        tester[5] = Calendar.getInstance();
        tester[5].setTimeInMillis(System.currentTimeMillis());
        tester[5].set(Calendar.HOUR_OF_DAY, 13);
        tester[5].set(Calendar.MINUTE, 35);

        tester[6] = Calendar.getInstance();
        tester[6].setTimeInMillis(System.currentTimeMillis());
        tester[6].set(Calendar.HOUR_OF_DAY, 6);
        tester[6].set(Calendar.MINUTE, 35);

        return tester;
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}