package bassamalim.hidaya.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.CheckboxListviewAdapter;
import bassamalim.hidaya.replacements.FilteredRecyclerAdapter;
import bassamalim.hidaya.models.CheckboxListItem;

public class FilterDialog<VH extends RecyclerView.ViewHolder> {

    private final Context context;
    private PopupWindow popup;
    private final View view;
    private final SharedPreferences pref;
    private final Gson gson;
    private final FilteredRecyclerAdapter<VH> filteredAdapter;
    private final ImageButton filterIb;
    private final String[] strArr;
    private final boolean[] selected;
    private final String prefKey;
    private CheckboxListviewAdapter cbListAdapter;
    private List<CheckboxListItem> items;

    public FilterDialog(Context context, View view, String title, String[] strArr,
                        boolean[] selected, FilteredRecyclerAdapter<VH> filteredAdapter,
                        ImageButton filterIb, String prefKey) {
        this.context = context;
        this.view = view;
        this.strArr = strArr;
        this.selected = selected;
        this.filteredAdapter = filteredAdapter;
        this.filterIb = filterIb;
        this.prefKey = prefKey;

        pref = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();

        showPopup(title);
    }

    private void showPopup(String title) {
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

        ((TextView) popup.getContentView().findViewById(R.id.dialog_title_tv)).setText(title);
        setupListview();
        setListeners();
    }

    private void setupListview() {
        items = new ArrayList<>();
        for (int i = 0; i < strArr.length; i++)
            items.add(new CheckboxListItem(strArr[i], selected[i]));

        ListView listView = popup.getContentView().findViewById(R.id.listview);
        cbListAdapter = new CheckboxListviewAdapter(context, 0, items, selected);
        listView.setAdapter(cbListAdapter);
    }

    private void setListeners() {
        popup.getContentView().findViewById(R.id.select_all_btn).setOnClickListener(v -> {
            for (int i = 0; i < items.size(); i++)
                items.get(i).setSelected(true);
            cbListAdapter.notifyDataSetChanged();

            Arrays.fill(selected, true);
        });
        popup.getContentView().findViewById(R.id.unselect_all_btn).setOnClickListener(v -> {
            for (int i = 0; i < items.size(); i++)
                items.get(i).setSelected(false);
            cbListAdapter.notifyDataSetChanged();

            Arrays.fill(selected, false);
        });

        popup.getContentView().findViewById(R.id.finish_btn)
                .setOnClickListener(v -> popup.dismiss());

        popup.setOnDismissListener(() -> {
            save();
            setFilterIb();
        });
    }

    private void setFilterIb() {
        boolean changed = false;
        for (boolean bool : selected) {
            if (!bool) {
                changed = true;
                break;
            }
        }
        if (changed)
            filterIb.setImageDrawable(AppCompatResources.getDrawable(
                    context, R.drawable.ic_filtered));
        else
            filterIb.setImageDrawable(AppCompatResources.getDrawable(
                    context, R.drawable.ic_filter));
    }

    private void save() {
        String str = gson.toJson(selected);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(prefKey, str);
        editor.apply();

        if (filteredAdapter != null)
            filteredAdapter.filter(null, selected);
    }
}
