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

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.DataSaver;
import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.MyNotification;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.databinding.PrayersFragmentBinding;
import com.bassamalim.athkar.receivers.AlarmsReceiver;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class PrayersFragment extends Fragment {

    private PrayersViewModel prayersViewModel;
    private PrayersFragmentBinding binding;
    private Location location;
    public static ArrayList<String> times;
    public Calendar[] formattedTimes;
    public static String[] translatedTimes;
    private Calendar calendar;
    private Date now;
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

        MyNotification notification = new MyNotification(formattedTimes);

        storeTimes(formatTimes(times));

        setAlarms();

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
        now = new Date();

        calendar = Calendar.getInstance();
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
            formattedTimes[i] = Calendar.getInstance();

            formattedTimes[i].setTimeInMillis(System.currentTimeMillis());
            formattedTimes[i].set(Calendar.HOUR_OF_DAY, Integer.parseInt(givenTimes.get(i).substring(0, 2)));
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

    public void setAlarms() {
        for (int i = 0; i < formattedTimes.length; i++) {
            int prayer = i;

            Intent intent = new Intent(getContext().getApplicationContext(), AlarmsReceiver.class);
            intent.putExtra("prayer", prayer);

            PendingIntent pendIntent = PendingIntent.getBroadcast(getContext().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager myAlarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

            myAlarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, formattedTimes[i].getTimeInMillis(), pendIntent);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}