package bassamalim.hidaya.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.CheckboxListItem;

public class CheckboxSpinnerAdapter extends ArrayAdapter<CheckboxListItem> {

    private final Context context;
    private final SharedPreferences pref;
    private final String prefKey;
    private final Gson gson;
    private final List<CheckboxListItem> items;
    private final boolean[] selected;
    private boolean isFromView = false;

    public CheckboxSpinnerAdapter(@NonNull Context context, int resource, @NonNull
            List<CheckboxListItem> objects, boolean[] selected, String prefKey) {
        super(context, resource, objects);
        this.context = context;
        this.items = objects;
        this.selected = selected;
        this.prefKey = prefKey;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();
    }

    private static class ViewHolder {
        private final TextView tv;
        private final CheckBox cb;

        public ViewHolder(View view) {
            tv = view.findViewById(R.id.text_tv);
            cb = view.findViewById(R.id.checkbox);
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView);
    }

    public View getCustomView(final int position, View convertView) {
        ViewHolder vh;
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.item_checkbox_list, null);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        }
        else
            vh = (ViewHolder) convertView.getTag();

        vh.tv.setText(items.get(position).getText());

        // To check weather checked event fire from getView() or user input
        isFromView = true;
        vh.cb.setChecked(items.get(position).isSelected());
        isFromView = false;

        if ((position == 0))
            vh.cb.setVisibility(View.INVISIBLE);
        else
            vh.cb.setVisibility(View.VISIBLE);

        if (position != 0) {
            vh.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isFromView) {
                    selected[position-1] = isChecked;
                    items.get(position).setSelected(isChecked);
                    updatePref();
                }
            });
        }

        return convertView;
    }

    private void updatePref() {
        String str = gson.toJson(selected);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(prefKey, str);
        editor.apply();
    }
}
