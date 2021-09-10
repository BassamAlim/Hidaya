package com.bassamalim.athkar.ui;

import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.PrayersFragmentBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class PrayersFragment extends Fragment {

    private PrayersFragmentBinding binding;
    private Location location;
    private Calendar[] timesArr;
    private ArrayList<String> times;
    private ArrayList<String> formattedTimes;
    private Calendar tomorrowFajr;
    private final CardView[] cards = new CardView[6];
    private final TextView[] screens = new TextView[6];
    private final TextView[] counters = new TextView[6];
    private CountDownTimer timer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = PrayersFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        location = MainActivity.location;
        timesArr = getTimesArr(MainActivity.times);
        times = getTimes();
        formatTimes();

        setViews();
        show();

        setupCountdown();

        return root;
    }

    private Calendar[] getTimesArr(Calendar[] given) {
        Calendar[] result = new Calendar[given.length-1];
        for (int i = 0; i < given.length; i++) {
            if (i<4)
                result[i] = given[i];
            else if (i>4)
                result[i-1] = given[i];
        }
        return result;
    }

    private ArrayList<String> getTimes() {
        PrayTimes prayTimes = new PrayTimes();

        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        tomorrowFajr = prayTimes.getTomorrowFajr(calendar, location.getLatitude(),
                location.getLongitude(), timezone);

        return prayTimes.getPrayerTimes(calendar, location.getLatitude(),
                location.getLongitude(), timezone);
    }

    private void formatTimes() {
        ArrayList<String> formatted = new ArrayList<>();
        formatted.add(0, "الفجر: " + times.get(0));
        formatted.add(1, "الشروق: " + times.get(1));
        formatted.add(2, "الظهر: " + times.get(2));
        formatted.add(3, "العصر: " + times.get(3));
        formatted.add(4, "المغرب: " + times.get(5));
        formatted.add(5, "العشاء: " + times.get(6));
        formattedTimes = formatted;
    }

    private void setViews() {
        cards[0] = binding.fajrCard;
        cards[1] = binding.shorouqCard;
        cards[2] = binding.dhuhrCard;
        cards[3] = binding.asrCard;
        cards[4] = binding.maghribCard;
        cards[5] = binding.ishaaCard;

        screens[0] = binding.fajrScreen;
        screens[1] = binding.shorouqScreen;
        screens[2] = binding.dhuhrScreen;
        screens[3] = binding.asrScreen;
        screens[4] = binding.maghribScreen;
        screens[5] = binding.ishaaScreen;

        counters[0] = binding.fajrCounter;
        counters[1] = binding.shorouqCounter;
        counters[2] = binding.dhuhrCounter;
        counters[3] = binding.asrCounter;
        counters[4] = binding.maghribCounter;
        counters[5] = binding.ishaaCounter;
    }

    private void show() {
        for (int i=0; i<formattedTimes.size(); i++)
            screens[i].setText(formattedTimes.get(i));
    }

    private void setupCountdown() {
        int coming = findClosest();
        count(coming);
    }

    private void count(int i) {
        boolean tomorrow = false;
        if (i == -1) {
            tomorrow = true;
            i = 0;
        }

        TextView screen = screens[i];
        TextView counter = counters[i];
        counter.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams up = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        up.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        screen.setLayoutParams(up);

        long till = timesArr[i].getTimeInMillis();
        if (tomorrow)
            till = tomorrowFajr.getTimeInMillis();

        int finalI = i;
        timer = new CountDownTimer(till - System.currentTimeMillis(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (60 * 60 * 1000) % 24;
                long minutes = millisUntilFinished / (60 * 1000) % 60;
                long seconds = millisUntilFinished / 1000 % 60;

                String hms = String.format(Locale.US, "%02d:%02d:%02d",
                        hours, minutes, seconds);
                counter.setText(String.format(getString(R.string.remaining), translateNumbers(hms)));
            }
            @Override
            public void onFinish() {
                up.gravity = Gravity.CENTER;
                screens[finalI].setLayoutParams(up);
                counter.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private int findClosest() {
        int closest = -1;
        long currentMillis = System.currentTimeMillis();
        for (int i=0; i<timesArr.length; i++) {
            long millis = timesArr[i].getTimeInMillis();
            if (millis > currentMillis) {
                closest = i;
                break;
            }
        }
        return closest;
    }

    private void cancelTimer() {
        if (timer != null)
            timer.cancel();
    }

    private String translateNumbers(String english) {
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
        StringBuilder temp = new StringBuilder();
        for (int j = 0; j < english.length(); j++) {
            char t = english.charAt(j);
            if (map.containsKey(t))
                t = map.get(t);
            temp.append(t);
        }
        return temp.toString();
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        cancelTimer();
    }
}