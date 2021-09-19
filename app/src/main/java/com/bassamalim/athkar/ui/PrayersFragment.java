package com.bassamalim.athkar.ui;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.PrayTimes;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.PrayersFragmentBinding;
import com.bassamalim.athkar.dialogs.PrayerPopup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class PrayersFragment extends Fragment {

    private PrayersFragmentBinding binding;
    private Location location;
    private Calendar[] times;
    private ArrayList<String> formattedTimes;
    private Calendar tomorrowFajr;
    private final CardView[] cards = new CardView[6];
    private final TextView[] screens = new TextView[6];
    private final TextView[] counters = new TextView[6];
    public static final ImageView[] images = new ImageView[6];
    private CountDownTimer timer;
    private SharedPreferences pref;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = PrayersFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext());

        location = MainActivity.location;
        getTimes();
        formatTimes();

        setViews();
        setInitialState();
        show();

        setupCountdown();

        setListeners();

        return root;
    }

    private void getTimes() {
        PrayTimes prayTimes = new PrayTimes();

        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        times = prayTimes.getPrayerTimesArray(calendar, location.getLatitude(),
                location.getLongitude(), timezone);

        formattedTimes = prayTimes.getPrayerTimes(calendar, location.getLatitude(),
                location.getLongitude(), timezone);

        tomorrowFajr = prayTimes.getTomorrowFajr(calendar, location.getLatitude(),
                location.getLongitude(), timezone);
    }

    private void formatTimes() {
        formattedTimes.set(0, "الفجر: " + formattedTimes.get(0));
        formattedTimes.set(1, "الشروق: " + formattedTimes.get(1));
        formattedTimes.set(2, "الظهر: " + formattedTimes.get(2));
        formattedTimes.set(3, "العصر: " + formattedTimes.get(3));
        formattedTimes.set(4, "المغرب: " + formattedTimes.get(4));
        formattedTimes.set(5, "العشاء: " + formattedTimes.get(5));
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
        screens[2] = binding.duhrScreen;
        screens[3] = binding.asrScreen;
        screens[4] = binding.maghribScreen;
        screens[5] = binding.ishaaScreen;

        counters[0] = binding.fajrCounter;
        counters[1] = binding.shorouqCounter;
        counters[2] = binding.duhrCounter;
        counters[3] = binding.asrCounter;
        counters[4] = binding.maghribCounter;
        counters[5] = binding.ishaaCounter;

        images[0] = binding.fajrImage;
        images[1] = binding.shorouqImage;
        images[2] = binding.duhrImage;
        images[3] = binding.asrImage;
        images[4] = binding.maghribImage;
        images[5] = binding.ishaaImage;
    }

    private void setInitialState() {
        for (int i = 0; i < images.length; i++) {
            int state = pref.getInt(i + "notification_type", 2);
            if (state == 1)
                images[i].setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),
                        R.drawable.ic_mute, requireContext().getTheme()));
            else if (state == 0)
                images[i].setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),
                        R.drawable.ic_disabled, requireContext().getTheme()));
        }
    }

    private void setListeners() {
        for (int i=0; i< cards.length; i++) {
            int finalI = i;
            cards[i].setOnClickListener(v -> new PrayerPopup(getContext(), v, finalI));
        }
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
        up.gravity = Gravity.TOP | Gravity.START;
        screen.setLayoutParams(up);

        long till = times[i].getTimeInMillis();
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
                up.gravity = Gravity.CENTER | Gravity.END;
                screens[finalI].setLayoutParams(up);
                counter.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private int findClosest() {
        int closest = -1;
        long currentMillis = System.currentTimeMillis();
        for (int i=0; i<times.length; i++) {
            long millis = times[i].getTimeInMillis();
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