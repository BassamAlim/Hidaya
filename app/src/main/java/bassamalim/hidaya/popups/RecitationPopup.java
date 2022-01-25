package bassamalim.hidaya.popups;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;

public class RecitationPopup {

    private final Context context;
    private PopupWindow popupWindow;
    private final View view;
    private final SharedPreferences pref;
    private List<String> reciters;
    private Spinner recitersSpinner;
    private Button[] buttons;
    private SwitchCompat surahSwitch;
    private SwitchCompat pageSwitch;

    public RecitationPopup(Context gContext, View v) {
        context = gContext;
        view = v;
        pref = PreferenceManager.getDefaultSharedPreferences(context);

        showPopup();
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup_recitation,
                new LinearLayout(context), false);

        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.RecitationPopupAnimation);

        popupWindow.showAtLocation(view, Gravity.CENTER|Gravity.BOTTOM, 0, 150);

        setViews();
        setInitialState();
        reciters = getReciterNames();
        setListeners();
        setupSpinner();
    }

    private void setViews() {
        View cv = popupWindow.getContentView();

        recitersSpinner = cv.findViewById(R.id.reciters_spinner);
        surahSwitch = cv.findViewById(R.id.stop_on_surah);
        pageSwitch = cv.findViewById(R.id.stop_on_page);

        buttons = new Button[] {cv.findViewById(R.id.repeat_once),
                cv.findViewById(R.id.repeat_twice), cv.findViewById(R.id.repeat_three),
        cv.findViewById(R.id.repeat_forever)};
    }

    private void setInitialState() {
        surahSwitch.setChecked(pref.getBoolean("stop_on_surah", false));
        pageSwitch.setChecked(pref.getBoolean("stop_on_page", false));

        int repeat = pref.getInt("aya_repeat_mode", 0);

        buttons[repeat].setTextColor(context.getResources().getColor(R.color.accent));
    }

    private void setListeners() {
        surahSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("stop_on_surah", isChecked);
            editor.apply();
        });
        pageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("stop_on_page", isChecked);
            editor.apply();
        });

        for (int i = 0; i < 4; i++) {
            int finalI = i;
            buttons[i].setOnClickListener(v -> {
                updateTextColor(finalI);

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("aya_repeat_mode", finalI);
                editor.apply();
            });
        }
    }

    private void updateTextColor(int chosen) {
        for (int i = 0; i < 4; i++)
            buttons[i].setTextColor(context.getResources().getColor(R.color.text));

        buttons[chosen].setTextColor(context.getResources().getColor(R.color.accent));
    }

    private void setupSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, reciters);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        recitersSpinner.setAdapter(adapter);

        int lastChosen = pref.getInt("chosen_reciter", 13);
        recitersSpinner.setSelection(lastChosen);

        recitersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    private List<String> getReciterNames() {
        return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build().ayatRecitersDao().getNames();
    }

}
