package bassamalim.hidaya.fragments;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
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
import bassamalim.hidaya.dialogs.PrayerDialog;
import bassamalim.hidaya.dialogs.TutorialDialog;
import bassamalim.hidaya.helpers.PrayTimes;
import bassamalim.hidaya.other.Utils;

public class PrayersFragment extends Fragment {

    private FragmentPrayersBinding binding;
    private Location location;
    private String[] prayerNames;
    private Calendar[] times;
    private Calendar tomorrowFajr;
    private final CardView[] cards = new CardView[6];
    private final ConstraintLayout[] cls = new ConstraintLayout[6];
    private final TextView[] screens = new TextView[6];
    private final TextView[] counters = new TextView[6];
    private final ImageView[] images = new ImageView[6];
    private final TextView[] delayTvs = new TextView[6];
    private TextView dayScreen;
    private CountDownTimer timer;
    private SharedPreferences pref;
    private int currentChange = 0;
    private Calendar selectedDay;
    private int upcoming;
    private final ConstraintSet constraintSet = new ConstraintSet();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPrayersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext());

        if (MainActivity.located) {
            initiate();
            goToToday();
            setInitialState();
            setListeners();
        }

        checkFirstTime();

        return root;
    }

    private void initiate() {
        location = MainActivity.location;
        prayerNames = getResources().getStringArray(R.array.prayer_names);
        setViews();
    }

    private void setViews() {
        cards[0] = binding.fajrCard;
        cards[1] = binding.shorouqCard;
        cards[2] = binding.duhrCard;
        cards[3] = binding.asrCard;
        cards[4] = binding.maghribCard;
        cards[5] = binding.ishaaCard;

        cls[0] = binding.fajrCl;
        cls[1] = binding.shorouqCl;
        cls[2] = binding.duhrCl;
        cls[3] = binding.asrCl;
        cls[4] = binding.maghribCl;
        cls[5] = binding.ishaaCl;

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

        delayTvs[0] = binding.fajrDelayTv;
        delayTvs[1] = binding.shorouqDelayTv;
        delayTvs[2] = binding.duhrDelayTv;
        delayTvs[3] = binding.asrDelayTv;
        delayTvs[4] = binding.maghribDelayTv;
        delayTvs[5] = binding.ishaaDelayTv;

        dayScreen = binding.dayScreen;
    }

    private void goToToday() {
        currentChange = 0;
        getTimes(0);
        updateDayScreen();
        count();
    }

    /**
     * It gets the prayer times for the current day and the next day, and sets the text of the
     * prayer time screens to the prayer times
     *
     * @param change The number of days to add to the current date.
     */
    private void getTimes(int change) {
        PrayTimes prayTimes = new PrayTimes(getContext());

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

        for (int i = 0; i < formattedTimes.size(); i++) {
            String text = prayerNames[i] + ": " + formattedTimes.get(i);
            screens[i].setText(text);

            times[i].set(Calendar.SECOND, 0);
        }
    }

    private void setInitialState() {
        for (int i = 0; i < cards.length; i++) {
            int state = pref.getInt(Utils.mapID(i) + "notification_type", 2);
            if (state == 3)
                images[i].setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                        .getResources(), R.drawable.ic_speaker, requireContext().getTheme()));
            else if (state == 1)
                images[i].setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                        .getResources(), R.drawable.ic_silent, requireContext().getTheme()));
            else if (state == 0)
                images[i].setImageDrawable(ResourcesCompat.getDrawable(requireContext()
                        .getResources(), R.drawable.ic_disabled, requireContext().getTheme()));

            int delayPosition = pref.getInt(Utils.mapID(i) + "spinner_last", 6);
            int min = Integer.parseInt(getResources().getStringArray(
                    R.array.time_settings_values)[delayPosition]);
            if (min > 0) {
                String positive = Utils.translateNumbers(getContext(), "+" + min);
                delayTvs[i].setText(positive);
            }
            else if (min < 0)
                delayTvs[i].setText(Utils.translateNumbers(getContext(), String.valueOf(min)));
            else
                delayTvs[i].setText("");
        }
    }

    private void setListeners() {
        for (int i=0; i< cards.length; i++) {
            int finalI = i;
            cards[i].setOnClickListener(v -> new PrayerDialog(getContext(), v,
                    Utils.mapID(finalI), prayerNames[finalI]));
        }

        binding.previousDayButton.setOnClickListener(v -> previousDay());
        binding.nextDayButton.setOnClickListener(v -> nextDay());
        binding.dayScreen.setOnClickListener(v -> goToToday());
    }

    private void checkFirstTime() {
        if (pref.getBoolean("is_first_time_in_prayers", true))
            new TutorialDialog(getContext(),
                    getString(R.string.prayers_tips),
                    "is_first_time_in_prayers").show(requireActivity()
                    .getSupportFragmentManager(), TutorialDialog.TAG);
    }

    private void count() {
        upcoming = findUpcoming();

        boolean tomorrow = false;
        if (upcoming == -1) {
            tomorrow = true;
            upcoming = 0;
        }

        counters[upcoming].setVisibility(View.VISIBLE);
        constraintSet.clone(cls[upcoming]);
        constraintSet.clear(screens[upcoming].getId(), ConstraintSet.BOTTOM);
        constraintSet.applyTo(cls[upcoming]);

        long till = times[upcoming].getTimeInMillis();
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

                if (getContext() != null)
                    counters[upcoming].setText(String.format(getString(R.string.remaining),
                            Utils.translateNumbers(requireContext(), hms)));
                else
                    cancelTimer();
            }
            @Override
            public void onFinish() {
                constraintSet.connect(screens[upcoming].getId(), ConstraintSet.BOTTOM,
                        cls[upcoming].getId(), ConstraintSet.BOTTOM);
                constraintSet.applyTo(cls[upcoming]);
                counters[upcoming].setVisibility(View.GONE);

                count();
            }
        }.start();
    }

    private int findUpcoming() {
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
            dayScreen.setText(getString(R.string.day));
        else {
            String text = "";

            Calendar hijri = new UmmalquraCalendar();
            hijri.setTime(selectedDay.getTime());

            String year = " " + hijri.get(Calendar.YEAR);
            String month = " " +
                    getResources().getStringArray(R.array.hijri_months)[Calendar.MONTH];
            String day = "" + hijri.get(Calendar.DATE);

            text += Utils.translateNumbers(getContext(), day) + month +
                    Utils.translateNumbers(getContext(), year);

            dayScreen.setText(text);
        }
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            constraintSet.connect(screens[upcoming].getId(), ConstraintSet.BOTTOM,
                    cls[upcoming].getId(), ConstraintSet.BOTTOM);
            constraintSet.applyTo(cls[upcoming]);
            counters[upcoming].setVisibility(View.GONE);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        cancelTimer();
    }
}