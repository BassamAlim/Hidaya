package com.bassamalim.athkar.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.helpers.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.R;

public class PrayerPopup extends AppCompatActivity {

    private final Context context;
    private PopupWindow popupWindow;
    private final View view;
    private final int id;
    private final String name;
    private final SharedPreferences pref;
    private Button[] buttons;
    private Spinner spinner;
    private ImageView[] images;

    public PrayerPopup(Context gContext, View v, int gId, String gName) {
        context = gContext;
        view = v;
        id = gId;
        name = gName;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        showPopup();
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.prayer_popup,
                new LinearLayout(context), false);

        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
        popupWindow.setOutsideTouchable(true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        populate();
    }

    private void populate() {
        TextView nameScreen = popupWindow.getContentView().findViewById(R.id.popup_prayer_name);
        String temp = "إعدادات " + name;
        nameScreen.setText(temp);

        buttons = new Button[3];
        buttons[0] = popupWindow.getContentView().findViewById(R.id.disable_button);
        buttons[1] = popupWindow.getContentView().findViewById(R.id.mute_button);
        buttons[2] = popupWindow.getContentView().findViewById(R.id.sound_button);

        spinner = popupWindow.getContentView().findViewById(R.id.time_setting_spinner);

        setImages();

        int state = pref.getInt(id + "notification_type", 2);
        selectedState(state);

        setListeners();
        setupSpinner();
    }

    private void setListeners() {
        SharedPreferences.Editor editor = pref.edit();
        buttons[2].setOnClickListener(v -> {
            images[id].setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),
                    R.drawable.ic_sound, context.getTheme()));

            new Alarms(context, id);

            editor.putInt(id +"notification_type", 2);
            selectedState(2);
            editor.apply();
        });
        buttons[1].setOnClickListener(v -> {
            images[id].setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),
                    R.drawable.ic_mute, context.getTheme()));

            new Alarms(context, id);

            editor.putInt(id +"notification_type", 1);
            selectedState(1);
            editor.apply();
        });
        buttons[0].setOnClickListener(v -> {
            images[id].setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),
                    R.drawable.ic_disabled, context.getTheme()));

            Alarms.cancelAlarm(context, id);

            editor.putInt(id +"notification_type", 0);
            selectedState(0);
            editor.apply();
        });
    }

    private void setupSpinner() {
        SharedPreferences.Editor editor = pref.edit();

        spinner = popupWindow.getContentView().findViewById(R.id.time_setting_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.time_settings, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        int time = pref.getInt(id +"spinner_last", 4);
        spinner.setSelection(time);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long vId) {
                int min = Integer.parseInt(parent.getItemAtPosition(position).toString());
                Log.i(Constants.TAG, "delay is set to: " + min);

                long millis = min * 60000L;

                new Alarms(context, id);

                editor.putLong(id +"time_adjustment", millis);
                editor.putInt(id + "spinner_last", position);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(listener);
    }

    private void selectedState(int choice) {
        buttons[0].setTextColor(context.getResources().getColor(R.color.white));
        buttons[1].setTextColor(context.getResources().getColor(R.color.white));
        buttons[2].setTextColor(context.getResources().getColor(R.color.white));

        buttons[choice].setTextColor(context.getResources().getColor(R.color.accent));
    }

    private void setImages() {
        images = new ImageView[6];
        images[0] = view.findViewById(R.id.fajr_image);
        images[1] = view.findViewById(R.id.shorouq_image);
        images[2] = view.findViewById(R.id.duhr_image);
        images[3] = view.findViewById(R.id.asr_image);
        images[4] = view.findViewById(R.id.maghrib_image);
        images[5] = view.findViewById(R.id.ishaa_image);
    }

}
