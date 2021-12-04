package com.bassamalim.hidaya.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bassamalim.hidaya.R;
import com.bassamalim.hidaya.activities.QuranActivity;
import com.bassamalim.hidaya.models.SurahButton;

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
        View view = LayoutInflater.from(context).inflate(R.layout.surah_button_row_item,
                viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getButton().setText(surahButtons.get(position).getSurahName());

        String tanzeel = surahButtons.get(position).getTanzeel();

        Drawable d = null;
        if (tanzeel.equals("Meccan"))
            d = AppCompatResources.getDrawable(context, R.drawable.ic_kaaba_black);
        else if (tanzeel.equals("Medinan"))
            d = AppCompatResources.getDrawable(context, R.drawable.ic_madinah);

        viewHolder.getButton().setCompoundDrawablesWithIntrinsicBounds(d,
                null, null, null);

        viewHolder.getButton().setOnClickListener(surahButtons.get(position).getListener());
    }

    @Override
    public int getItemCount() {
        return surahButtons.size();
    }

    public void filterName(String text) {
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

    public void filterNumber(String text) {
        surahButtons.clear();
        if (text.isEmpty())
            surahButtons.addAll(surahButtonsCopy);
        else {
            try {
                int num = Integer.parseInt(text);
                if (num >= 1 && num <= 604) {
                    Intent openPage = new Intent(context, QuranActivity.class);
                    openPage.setAction("by_page");
                    openPage.putExtra("page", num);
                    context.startActivity(openPage);
                }
                else
                    Toast.makeText(context, "لا توجد صفحة بهذا الرقم", Toast.LENGTH_SHORT).show();
            }
            catch (NumberFormatException e) {
                for(SurahButton surahButton: surahButtonsCopy) {
                    if(surahButton.getSearchName().contains(text))
                        surahButtons.add(surahButton);
                }
            }
        }
        notifyDataSetChanged();
    }

}
