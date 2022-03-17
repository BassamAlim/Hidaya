package bassamalim.hidaya.popups;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import bassamalim.hidaya.R;
import bassamalim.hidaya.enums.ID;
import bassamalim.hidaya.helpers.Alarms;
import bassamalim.hidaya.other.Const;
import bassamalim.hidaya.other.Utils;

public class PrayerPopup {

    private final Context context;
    private PopupWindow popup;
    private final View view;
    private final ID id;
    private final String name;
    private final SharedPreferences pref;
    private Button disableBtn;
    private Button muteBtn;
    private Button notifyBtn;
    private Button athanBtn;
    private ImageView[] images;

    public PrayerPopup(Context gContext, View v, ID id, String gName) {
        context = gContext;
        view = v;
        this.id = id;
        name = gName;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        showPopup();
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup_prayer,
                new LinearLayout(context), false);

        if (id == ID.SHOROUQ) {
            popupView.findViewById(R.id.athan_button).setVisibility(View.GONE);
            popupView.findViewById(R.id.first_separator).setVisibility(View.GONE);
        }

        popup = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setAnimationStyle(R.style.PrayerPopupAnimation);

        popup.showAtLocation(view, Gravity.START, 30, getY());

        populate();
    }

    private void populate() {
        TextView nameScreen = popup.getContentView().findViewById(R.id.popup_prayer_name);
        String temp = "إعدادات " + name;
        nameScreen.setText(temp);

        disableBtn = popup.getContentView().findViewById(R.id.disable_button);
        muteBtn = popup.getContentView().findViewById(R.id.mute_button);
        notifyBtn = popup.getContentView().findViewById(R.id.notify_button);
        athanBtn = popup.getContentView().findViewById(R.id.athan_button);

        setViews();

        int defaultState = id == ID.SHOROUQ ? 0 : 2;
        int state = pref.getInt(id+"notification_type", defaultState);
        selectedState(state);

        setListeners();
        setupSpinner();
    }

    private void setListeners() {
        athanBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            images[id.ordinal()].setImageDrawable(ResourcesCompat.getDrawable(
                    context.getResources(), R.drawable.ic_speaker, context.getTheme()));

            new Alarms(context, id);

            editor.putInt(id+"notification_type", 3);
            selectedState(3);
            editor.apply();
        });
        notifyBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            images[id.ordinal()].setImageDrawable(ResourcesCompat.getDrawable(
                    context.getResources(), R.drawable.ic_sound, context.getTheme()));

            new Alarms(context, id);

            editor.putInt(id+"notification_type", 2);
            selectedState(2);
            editor.apply();
        });
        muteBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            images[id.ordinal()].setImageDrawable(ResourcesCompat.getDrawable(
                    context.getResources(), R.drawable.ic_mute, context.getTheme()));

            new Alarms(context, id);

            editor.putInt(id+"notification_type", 1);
            selectedState(1);
            editor.apply();
        });
        disableBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            images[id.ordinal()].setImageDrawable(ResourcesCompat.getDrawable(
                    context.getResources(), R.drawable.ic_disabled, context.getTheme()));

            Utils.cancelAlarm(context, id);

            editor.putInt(id+"notification_type", 0);
            selectedState(0);
            editor.apply();
        });
    }

    private void setupSpinner() {
        Spinner spinner = popup.getContentView().findViewById(R.id.time_setting_spinner);

        int time = pref.getInt(id +"spinner_last", 6);
        spinner.setSelection(time);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long vId) {
                SharedPreferences.Editor editor = pref.edit();
                int min = Integer.parseInt(context.getResources().getStringArray(
                        R.array.time_settings_values)[parent.getSelectedItemPosition()]);
                Log.i(Const.TAG, "delay is set to: " + min);

                long millis = min * 60000L;

                new Alarms(context, id);

                editor.putLong(id +"time_adjustment", millis);
                editor.putInt(id + "spinner_last", position);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void selectedState(int choice) {
        TypedValue value = new TypedValue();

        context.getTheme().resolveAttribute(R.attr.myText, value, true);
        athanBtn.setTextColor(value.data);
        notifyBtn.setTextColor(value.data);
        muteBtn.setTextColor(value.data);
        disableBtn.setTextColor(value.data);

        context.getTheme().resolveAttribute(R.attr.myAccent, value, true);
        int color = value.data;

        if (pref.getString(context.getString(R.string.theme_key), "ThemeM")
                .equals("ThemeL"))
            color = Color.WHITE;

        switch (choice) {
            case 0:
                disableBtn.setTextColor(color);
                break;
            case 1:
                muteBtn.setTextColor(color);
                break;
            case 2:
                notifyBtn.setTextColor(color);
                break;
            case 3:
                athanBtn.setTextColor(color);
        }
    }

    private void setViews() {
        images = new ImageView[6];
        images[0] = view.findViewById(R.id.fajr_image);
        images[1] = view.findViewById(R.id.shorouq_image);
        images[2] = view.findViewById(R.id.duhr_image);
        images[3] = view.findViewById(R.id.asr_image);
        images[4] = view.findViewById(R.id.maghrib_image);
        images[5] = view.findViewById(R.id.ishaa_image);
    }

    private int getY() {
        switch (id) {
            case FAJR: return  -400;
            case SHOROUQ: return -350;
            case DUHR: return -180;
            case ASR: return 100;
            case MAGHRIB: return 350;
            case ISHAA: return 510;
        }
        return 0;
    }
}
