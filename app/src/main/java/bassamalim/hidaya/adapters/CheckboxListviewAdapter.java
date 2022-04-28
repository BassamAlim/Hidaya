package bassamalim.hidaya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.CheckboxListItem;

public class CheckboxListviewAdapter extends ArrayAdapter<CheckboxListItem> {

    private final Context context;
    private final List<CheckboxListItem> items;
    private final boolean[] selected;
    private boolean isFromView;

    public CheckboxListviewAdapter(@NonNull Context context, int resource, @NonNull
            List<CheckboxListItem> objects, boolean[] selected) {
        super(context, resource, objects);
        this.context = context;
        this.items = objects;
        this.selected = selected;
    }

    private static class ViewHolder {
        private final TextView tv;
        private final CheckBox cb;

        public ViewHolder(View view) {
            tv = view.findViewById(R.id.text_tv);
            cb = view.findViewById(R.id.checkbox);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View result;
        ViewHolder vh;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.item_checkbox_list, null);
            vh = new ViewHolder(convertView);
            result = convertView;
            convertView.setTag(vh);
        }
        else {
            vh = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        CheckboxListItem item = items.get(position);
        vh.tv.setText(item.getText());

        isFromView = true;
        vh.cb.setChecked(item.isSelected());
        isFromView = false;

        vh.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isFromView) {
                item.setSelected(isChecked);
                selected[position] = isChecked;
            }
        });

        return result;
    }

}
