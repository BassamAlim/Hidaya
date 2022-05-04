package bassamalim.hidaya.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import bassamalim.hidaya.R;
import bassamalim.hidaya.enums.ID;
import bassamalim.hidaya.helpers.Alarms;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;

public class PrayerDialog {

    private final Context context;
    private PopupWindow popup;
    private final View view;
    private final ID id;
    private final SharedPreferences pref;
    private RadioGroup radioGroup;
    private RadioButton[] rButtons;
    private ImageView[] images;
    private TextView[] delayTvs;
    private int[] drawables;

    public PrayerDialog(Context context, View view, ID id, String title) {
        this.context = context;
        this.view = view;
        this.id = id;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        showPopup();

        populate(title);
    }

    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.dialog_prayer,
                new LinearLayout(context), false);

        if (id == ID.SHOROUQ)
            popupView.findViewById(R.id.athan_rb).setVisibility(View.GONE);

        popup = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popup.setElevation(10);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setAnimationStyle(R.style.PrayerDialogAnimation);

        popup.showAtLocation(view, Gravity.START, 30, getY());
    }

    @SuppressLint("StringFormatInvalid")
    private void populate(String title) {
        TextView nameScreen = popup.getContentView().findViewById(R.id.prayer_name_tv);
        nameScreen.setText(String.format(context.getString(R.string.settings_of), title));

        setViews();

        int defaultState = id == ID.SHOROUQ ? 0 : 2;
        int state = pref.getInt(id+"notification_type", defaultState);
        radioGroup.check(rButtons[state].getId());

        radioGroup.setOnCheckedChangeListener((group, checkedId) ->
                selectedAlertState(getIndex(checkedId)));

        setupSpinner();
    }

    private void setViews() {
        radioGroup = popup.getContentView().findViewById(R.id.prayer_alert_rg);

        rButtons = new RadioButton[4];
        rButtons[0] = popup.getContentView().findViewById(R.id.disable_rb);
        rButtons[1] = popup.getContentView().findViewById(R.id.silent_rb);
        rButtons[2] = popup.getContentView().findViewById(R.id.notify_rb);
        rButtons[3] = popup.getContentView().findViewById(R.id.athan_rb);

        images = new ImageView[6];
        images[0] = view.findViewById(R.id.fajr_image);
        images[1] = view.findViewById(R.id.shorouq_image);
        images[2] = view.findViewById(R.id.duhr_image);
        images[3] = view.findViewById(R.id.asr_image);
        images[4] = view.findViewById(R.id.maghrib_image);
        images[5] = view.findViewById(R.id.ishaa_image);

        delayTvs = new TextView[6];
        delayTvs[0] = view.findViewById(R.id.fajr_delay_tv);
        delayTvs[1] = view.findViewById(R.id.shorouq_delay_tv);
        delayTvs[2] = view.findViewById(R.id.duhr_delay_tv);
        delayTvs[3] = view.findViewById(R.id.asr_delay_tv);
        delayTvs[4] = view.findViewById(R.id.maghrib_delay_tv);
        delayTvs[5] = view.findViewById(R.id.ishaa_delay_tv);

        drawables = new int[4];
        drawables[0] = R.drawable.ic_disabled;
        drawables[1] = R.drawable.ic_silent;
        drawables[2] = R.drawable.ic_sound;
        drawables[3] = R.drawable.ic_speaker;
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
                Log.i(Global.TAG, "delay is set to: " + min);

                if (min > 0) {
                    String positive = Utils.translateNumbers(context, "+" + min);
                    delayTvs[id.ordinal()].setText(positive);
                }
                else if (min < 0)
                    delayTvs[id.ordinal()].setText(Utils.translateNumbers(
                            context, String.valueOf(min)));
                else
                    delayTvs[id.ordinal()].setText("");

                new Alarms(context, id);

                long millis = min * 60000L;
                editor.putLong(id + "time_adjustment", millis);
                editor.putInt(id + "spinner_last", position);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void selectedAlertState(int choice) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(id+"notification_type", choice);
        editor.apply();

        images[id.ordinal()].setImageDrawable(ResourcesCompat.getDrawable(
                context.getResources(), drawables[choice], context.getTheme()));

        new Alarms(context, id);
    }

    private int getIndex(int checkedId) {
        for (int i = 0; i < rButtons.length; i++) {
            if (rButtons[i].getId() == checkedId)
                return i;
        }
        return 2;
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
