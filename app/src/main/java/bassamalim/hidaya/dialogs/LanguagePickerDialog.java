package bassamalim.hidaya.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.preference.PreferenceManager;

import bassamalim.hidaya.R;

public class LanguagePickerDialog {

    private final Context context;
    private PopupWindow popup;
    private final SharedPreferences pref;
    private ListView listView;

    public LanguagePickerDialog(Context context, View view) {
        this.context = context;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        showPopup(view);
    }

    private void showPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) view.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.dialog_filter,
                new LinearLayout(context), false);

        popup = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popup.setElevation(10);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(false);

        popup.showAtLocation(view, Gravity.CENTER, 0, 50);

        setupListview();
        setListeners();
    }

    private void setupListview() {
        listView = popup.getContentView().findViewById(R.id.listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.support_simple_spinner_dropdown_item,
                context.getResources().getStringArray(R.array.languages_values));
        listView.setAdapter(adapter);
    }

    private void setListeners() {
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(context.getResources().getString(R.string.language_key),
                        context.getResources().getStringArray(R.array.languages_values)[position]);
                editor.apply();

                popup.dismiss();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
