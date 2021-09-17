package com.bassamalim.athkar.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.Alarms;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.ui.PrayersFragment;

import java.util.concurrent.CompletionStage;

public class PrayerPopup extends AppCompatActivity {

    private final Context context;
    private PopupWindow popupWindow;
    private final View view;
    private final int id;
    private final SharedPreferences pref;
    private Button[] buttons;
    private Spinner spinner;

    public PrayerPopup(Context gContext, View v, int gId) {
        context = gContext;
        view = v;
        id = gId;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        showPopup();
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.prayer_popup, null);

        popupWindow = new PopupWindow(popupView, 700, 900, true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        setElements();

        /*popupView.setOnTouchListener((v, event) -> {
            //Close the window when clicked
            popupWindow.dismiss();
            v.performClick();
            return true;
        });*/
    }

    private void setElements() {
        buttons = new Button[3];
        buttons[0] = popupWindow.getContentView().findViewById(R.id.disable_button);
        buttons[1] = popupWindow.getContentView().findViewById(R.id.mute_button);
        buttons[2] = popupWindow.getContentView().findViewById(R.id.sound_button);

        spinner = popupWindow.getContentView().findViewById(R.id.time_setting_spinner);

        int state = pref.getInt(id + "notification_type", 2);
        selectedState(state);

        setListeners();
        setupSpinner();
    }

    private void setListeners() {
        SharedPreferences.Editor editor = pref.edit();
        buttons[2].setOnClickListener(v -> {
            PrayersFragment.toggles[id].setCompoundDrawables(AppCompatResources.getDrawable(
                    context, R.drawable.ic_sound), null, null, null);

            new Alarms(context, id);

            editor.putInt(id +"notification_type", 2);
            selectedState(2);
            editor.apply();
        });
        buttons[1].setOnClickListener(v -> {
            PrayersFragment.toggles[id].setCompoundDrawables(AppCompatResources.getDrawable(
                    context, R.drawable.ic_mute), null, null, null);

            new Alarms(context, id);

            editor.putInt(id +"notification_type", 1);
            selectedState(1);
            editor.apply();
        });
        buttons[0].setOnClickListener(v -> {
            PrayersFragment.toggles[id].setCompoundDrawables(AppCompatResources.getDrawable(
                    context, R.drawable.ic_disabled), null, null, null);

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

                new Alarms(context, id);

                editor.putLong(id +"time_delay", (long) min);
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

}
