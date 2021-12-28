package bassamalim.hidaya.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.QuranActivity;
import bassamalim.hidaya.models.SurahButton;
import bassamalim.hidaya.other.AppDatabase;
import bassamalim.hidaya.other.Constants;

public class SurahButtonAdapter extends RecyclerView.Adapter<SurahButtonAdapter.ViewHolder> {

    private Context context;
    private final ArrayList<SurahButton> surahButtons;
    private final ArrayList<SurahButton> surahButtonsCopy;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView button;

        public ViewHolder(View view) {
            super(view);

            button = view.findViewById(R.id.surah_button_model);
        }

        public CardView getButton() {
            return button;
        }
    }

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
        ((TextView) viewHolder.getButton().findViewById(R.id.namescreen))
                .setText(surahButtons.get(position).getSurahName());

        int tanzeel = surahButtons.get(position).getTanzeel();
        if (tanzeel == 0) // Makkah
            ((ImageView) viewHolder.getButton().findViewById(R.id.tanzeel_view)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_kaaba_black));
        else if (tanzeel == 1) // Madina
            ((ImageView) viewHolder.getButton().findViewById(R.id.tanzeel_view)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_madinah));

        int fav = surahButtons.get(position).getFavorite();
        if (fav == 0)
            ((ImageView) viewHolder.getButton().findViewById(R.id.fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_favorite));
        else if (fav == 1)
            ((ImageView) viewHolder.getButton().findViewById(R.id.fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_unfavorite));

        viewHolder.getButton().setOnClickListener(surahButtons.get(position).getCardListener());
        ((ImageButton) viewHolder.getButton().findViewById(R.id.fav_btn)).setOnClickListener(
                (View.OnClickListener) view -> {
                    AppDatabase db = Room.databaseBuilder(
                            context, AppDatabase.class, "HidayaDB").createFromAsset(
                                    "databases/HidayaDB.db").allowMainThreadQueries().build();
                    if (surahButtons.get(position).getFavorite() == 0) {
                        db.suraDao().setFav(position, 1);
                        surahButtons.get(position).setFavorite(1);
                    }
                    else if (surahButtons.get(position).getFavorite() == 1) {
                        db.suraDao().setFav(position, 0);
                        surahButtons.get(position).setFavorite(0);
                    }
                    notifyItemChanged(position);

                    Log.i(Constants.TAG, "changed:- position: " + position);
                });
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
