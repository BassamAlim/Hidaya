package com.bassamalim.athkar.popups;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.helpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecitationPopup {

    private final Context context;
    private PopupWindow popupWindow;
    private final View view;
    private final SharedPreferences pref;
    private ArrayList<String> reciters;

    public RecitationPopup(Context gContext, View v) {
        context = gContext;
        view = v;
        pref = PreferenceManager.getDefaultSharedPreferences(context);

        showPopup();
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.recitation_popup,
                new LinearLayout(context), false);

        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(10);
        popupWindow.setAnimationStyle(R.style.RecitationPopupAnimation);

        popupWindow.showAtLocation(view, Gravity.CENTER|Gravity.BOTTOM, 0, 150);

        getReciters();
        setListeners();
        setupSpinner();
    }

    private void setListeners() {
        SwitchCompat sw = popupWindow.getContentView().findViewById(R.id.continue_playing);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("play_next_page", isChecked);
            editor.apply();
        });
    }

    private void setupSpinner() {
        Spinner spinner = popupWindow.getContentView().findViewById(R.id.reciters_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, reciters);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        int lastChosen = pref.getInt("chosen_reciter", 13);
        spinner.setSelection(lastChosen);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long vId) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("chosen_reciter", position);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void getReciters() {
        reciters = new ArrayList<>();
        String json = Utils.getJsonFromAssets(context, "recitations.json");
        try {
            assert json != null;
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject reciter = arr.getJSONObject(i);
                String name = reciter.getString("name");
                reciters.add(name);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
