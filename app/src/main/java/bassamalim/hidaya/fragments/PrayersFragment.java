package bassamalim.hidaya.fragments;

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

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.MainActivity;
import bassamalim.hidaya.databinding.FragmentPrayersBinding;
import bassamalim.hidaya.helpers.PrayTimes;
import bassamalim.hidaya.other.Util;
import bassamalim.hidaya.popups.PrayerPopup;

public class PrayersFragment extends Fragment {

    private FragmentPrayersBinding binding;
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

        binding = FragmentPrayersBinding.inflate(inflater, container, false);
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
        cards[2] = binding.duhrCard;
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
            int state = pref.getInt(Util.mapID(i)+"notification_type", 2);
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
                    Util.mapID(finalI), prayerNames[finalI]));
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
                counter.setText(String.format(getString(R.string.remaining), Util.translateNumbers(hms)));
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

            String year = " " + hijri.get(Calendar.YEAR);
            String month = " " + Util.whichHijriMonth(hijri.get(Calendar.MONTH));
            String day = "" + hijri.get(Calendar.DATE);

            text += Util.translateNumbers(day) + month + Util.translateNumbers(year);

            dayScreen.setText(text);
        }
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

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        cancelTimer();
    }
}