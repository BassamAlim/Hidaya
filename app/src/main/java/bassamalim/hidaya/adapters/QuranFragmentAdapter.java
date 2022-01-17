package bassamalim.hidaya.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.gson.Gson;

import java.util.ArrayList;

import bassamalim.hidaya.R;
import bassamalim.hidaya.activities.QuranActivity;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.models.SurahCard;

public class QuranFragmentAdapter extends RecyclerView.Adapter<QuranFragmentAdapter.ViewHolder> {

    private final Context context;
    private final AppDatabase db;
    private final SharedPreferences pref;
    private final ArrayList<SurahCard> surahCards;
    private final ArrayList<SurahCard> surahCardsCopy;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.surah_button_model);
        }

        public CardView getCard() {
            return card;
        }
    }

    public QuranFragmentAdapter(Context context, ArrayList<SurahCard> buttons) {
        this.context = context;

        db = Room.databaseBuilder(context, AppDatabase.class, "HidayaDB").createFromAsset(
                "databases/HidayaDB.db").allowMainThreadQueries().build();

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        surahCards = new ArrayList<>(buttons);
        surahCardsCopy = new ArrayList<>(buttons);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_quran_fragment,
                viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        SurahCard card = surahCards.get(position);

        ((TextView) viewHolder.getCard().findViewById(R.id.namescreen))
                .setText(card.getSurahName());

        int tanzeel = card.getTanzeel();
        if (tanzeel == 0) // Makkah
            ((ImageView) viewHolder.getCard().findViewById(R.id.tanzeel_view)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_kaaba_black));
        else if (tanzeel == 1) // Madina
            ((ImageView) viewHolder.getCard().findViewById(R.id.tanzeel_view)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_madinah));

        int fav = card.getFavorite();
        if (fav == 0)
            ((ImageView) viewHolder.getCard().findViewById(R.id.alathkar_fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star_outline));
        else if (fav == 1)
            ((ImageView) viewHolder.getCard().findViewById(R.id.alathkar_fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star));

        viewHolder.getCard().setOnClickListener(card.getCardListener());
        viewHolder.getCard().findViewById(R.id.alathkar_fav_btn).setOnClickListener(
                view -> {
                    if (card.getFavorite() == 0) {
                        db.suraDao().setFav(card.getNumber(), 1);
                        card.setFavorite(1);
                    }
                    else if (card.getFavorite() == 1) {
                        db.suraDao().setFav(card.getNumber(), 0);
                        card.setFavorite(0);
                    }
                    notifyItemChanged(position);

                    updateFavorites();
                });
    }

    @Override
    public int getItemCount() {
        return surahCards.size();
    }

    public void filterName(String text) {
        surahCards.clear();
        if (text.isEmpty())
            surahCards.addAll(surahCardsCopy);
        else {
            for(SurahCard surahCard : surahCardsCopy) {
                if(surahCard.getSearchName().contains(text))
                    surahCards.add(surahCard);
            }
        }
        notifyDataSetChanged();
    }

    public void filterNumber(String text) {
        surahCards.clear();
        if (text.isEmpty())
            surahCards.addAll(surahCardsCopy);
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
                for(SurahCard surahCard : surahCardsCopy) {
                    if(surahCard.getSearchName().contains(text))
                        surahCards.add(surahCard);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void updateFavorites() {
        Object[] favSuras = db.suraDao().getFav().toArray();

        Gson gson = new Gson();
        String surasJson = gson.toJson(favSuras);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("favorite_suras", surasJson);
        editor.apply();
    }

}
