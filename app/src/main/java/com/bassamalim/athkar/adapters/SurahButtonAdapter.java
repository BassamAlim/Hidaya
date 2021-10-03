package com.bassamalim.athkar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.models.SurahButton;

import java.util.ArrayList;

public class SurahButtonAdapter extends RecyclerView.Adapter<SurahButtonAdapter.ViewHolder> {

    private Context context;
    private final ArrayList<SurahButton> surahButtons;
    private final ArrayList<SurahButton> surahButtonsCopy;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Button button;

        public ViewHolder(View view) {
            super(view);

            button = view.findViewById(R.id.surah_button_model);
        }

        public Button getButton() {
            return button;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param buttons containing the data to populate views to be used
     */
    public SurahButtonAdapter(ArrayList<SurahButton> buttons) {
        surahButtons = new ArrayList<>(buttons);
        surahButtonsCopy = new ArrayList<>(buttons);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.button_row_item,
                viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getButton().setText(surahButtons.get(position).getSurahName());

        String tanzeel = surahButtons.get(position).getTanzeel();
        if (tanzeel.equals("Meccan")) {
            viewHolder.getButton().setCompoundDrawables(null, null,
                    AppCompatResources.getDrawable(context, R.drawable.ic_kaaba), null);
        }
        else {
            viewHolder.getButton().setCompoundDrawables(null, null,
                    AppCompatResources.getDrawable(context, R.drawable.ic_madina), null);
        }

        viewHolder.getButton().setOnClickListener(surahButtons.get(position).getListener());
    }

    @Override
    public int getItemCount() {
        return surahButtons.size();
    }

    public void filter(String text) {
        surahButtons.clear();
        if (text.isEmpty())
            surahButtons.addAll(surahButtonsCopy);
        else {
            for(SurahButton surahButton: surahButtonsCopy) {
                if(surahButton.getSearchName().contains(text))
                    surahButtons.add(surahButton);
            }
        }
        notifyDataSetChanged();
    }

}
