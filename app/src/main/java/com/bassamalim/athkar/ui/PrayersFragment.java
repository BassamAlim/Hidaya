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

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.activities.MainActivity;
import com.bassamalim.athkar.databinding.PrayersFragmentBinding;
import com.bassamalim.athkar.popups.PrayerPopup;
import com.bassamalim.athkar.helpers.PrayTimes;
import com.bassamalim.athkar.other.ID;
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class PrayersFragment extends Fragment {

    private PrayersFragmentBinding binding;
    private Location location;
    private String[] prayerNames;
    private Calendar[] times;
    private Calendar tomorrowFajr;
    private final CardView[] cards = new CardView[6];
    private final TextView[] screens = new TextView[6];
    private final TextView[] counters = new TextView[6];
    private final ImageView[] images = new ImageView[6];
    private TextView dayScreen;
    private CountDownTimer timer;
    private SharedPreferences pref;
    private int currentChange = 0;
    private Calendar selectedDay;
    private int closest;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = PrayersFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (MainActivity.located) {
            initiate();

            goToToday();

            setInitialState();
            setListeners();
        }

        return root;
    }

    private void initiate() {
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext());

        location = MainActivity.location;

        prayerNames = new String[6];
        prayerNames[0] = "الفجر";
        prayerNames[1] = "الشروق";
        prayerNames[2] = "الظهر";
        prayerNames[3] = "العصر";
        prayerNames[4] = "المغرب";
        prayerNames[5] = "العشاء";

        setViews();
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

        dayScreen = binding.dayScreen;
    }

    private void goToToday() {
        currentChange = 0;
        getTimes(0);
        updateDayScreen();
        cancelTimer();
        count();
    }

    private void getTimes(int change) {
        PrayTimes prayTimes = new PrayTimes();

        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + change);

        selectedDay = calendar;

        TimeZone timeZoneObj = TimeZone.getDefault();
        long millis = timeZoneObj.getOffset(date.getTime());
        double timezone = millis / 3600000.0;

        times = prayTimes.getPrayerTimesArray(calendar, location.getLatitude(),
                location.getLongitude(), timezone);

        ArrayList<String> formattedTimes = prayTimes.getPrayerTimes(calendar,
                location.getLatitude(), location.getLongitude(), timezone);

        tomorrowFajr = prayTimes.getTomorrowFajr(calendar, location.getLatitude(),
                location.getLongitude(), timezone);
        tomorrowFajr.set(Calendar.SECOND, 0);

        for (int i=0; i<formattedTimes.size(); i++) {
            String text = prayerNames[i] + ": " + formattedTimes.get(i);
            screens[i].setText(text);

            times[i].set(Calendar.SECOND, 0);
        }
    }

    private void setInitialState() {
        for (int i = 0; i < images.length; i++) {
            int state = pref.getInt(mapID(i)+"notification_type", 2);
            if (state == 3)
                images[i].setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                        .getResources(), R.drawable.ic_speaker, requireContext().getTheme()));
            else if (state == 1)
                images[i].setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                        .getResources(), R.drawable.ic_mute, requireContext().getTheme()));
            else if (state == 0)
                images[i].setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                        .getResources(), R.drawable.ic_disabled, requireContext().getTheme()));
        }
    }

    private void setListeners() {
        for (int i=0; i< cards.length; i++) {
            int finalI = i;
            cards[i].setOnClickListener(v -> new PrayerPopup(getContext(), v,
                    mapID(finalI), prayerNames[finalI]));
        }

        binding.previousDayButton.setOnClickListener(v -> previousDay());
        binding.nextDayButton.setOnClickListener(v -> nextDay());
        binding.dayScreen.setOnClickListener(v -> goToToday());
    }

    private void count() {
        closest = findClosest();

        boolean tomorrow = false;
        if (closest == -1) {
            tomorrow = true;
            closest = 0;
        }

        TextView screen = screens[closest];
        TextView counter = counters[closest];
        counter.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams up = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        up.gravity = Gravity.TOP | Gravity.START;
        screen.setLayoutParams(up);

        long till = times[closest].getTimeInMillis();
        if (tomorrow)
            till = tomorrowFajr.getTimeInMillis();

        timer = new CountDownTimer(till - System.currentTimeMillis(),
                1000) {
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
                up.gravity = Gravity.CENTER | Gravity.START;
                screens[closest].setLayoutParams(up);
                counter.setVisibility(View.GONE);
            }
        }.start();
    }

    private int findClosest() {
        long currentMillis = System.currentTimeMillis();
        for (int i=0; i<times.length; i++) {
            long millis = times[i].getTimeInMillis();
            if (millis > currentMillis)
                return i;
        }
        return -1;
    }

    private void previousDay() {
        getTimes(--currentChange);
        updateDayScreen();
        cancelTimer();
        if (currentChange == 0)
            count();
    }

    private void nextDay() {
        getTimes(++currentChange);
        updateDayScreen();
        cancelTimer();
        if (currentChange == 0)
            count();
    }

    private void updateDayScreen() {
        if (currentChange == 0)
            dayScreen.setText("اليوم");
        else {
            String text = "";

            Calendar hijri = new UmmalquraCalendar();
            hijri.setTime(selectedDay.getTime());

            String day = String.valueOf(hijri.get(Calendar.DATE));
            String year = String.valueOf(hijri.get(Calendar.YEAR));
            String month = whichMonth(hijri.get(Calendar.MONTH));

            day = translateNumbers(day);
            year = translateNumbers(year);

            text += day + " " + month + " " + year;

            dayScreen.setText(translateNumbers(text));
        }
    }

    private String whichMonth(int num) {
        String result;
        HashMap<Integer, String> monthMap = new HashMap<>();
        monthMap.put(0, "مُحَرَّم");
        monthMap.put(1, "صَفَر");
        monthMap.put(2, "ربيع الأول");
        monthMap.put(3, "ربيع الثاني");
        monthMap.put(4, "جُمادى الأول");
        monthMap.put(5, "جُمادى الآخر");
        monthMap.put(6, "رجب");
        monthMap.put(7, "شعبان");
        monthMap.put(8, "رَمَضان");
        monthMap.put(9, "شَوَّال");
        monthMap.put(10, "ذو القِعْدة");
        monthMap.put(11, "ذو الحِجَّة");

        result = monthMap.get(num);
        return result;
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

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER | Gravity.START;
            counters[closest].setVisibility(View.GONE);
            screens[closest].setLayoutParams(params);
        }
    }

    private ID mapID(int num) {
        switch (num) {
            case 0: return ID.FAJR;
            case 1: return ID.SHOROUQ;
            case 2: return ID.DUHR;
            case 3: return ID.ASR;
            case 4: return ID.MAGHRIB;
            case 5: return ID.ISHAA;
            default: return null;
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        cancelTimer();
    }
}