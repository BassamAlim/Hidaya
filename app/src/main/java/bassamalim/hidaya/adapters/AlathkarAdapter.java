package bassamalim.hidaya.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.gson.Gson;

import java.util.ArrayList;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.models.AlathkarButton;

public class AlathkarAdapter extends RecyclerView.Adapter<AlathkarAdapter.ViewHolder> {

    private final Context context;
    private final AppDatabase db;
    private final ArrayList<AlathkarButton> alathkarCards;
    private final ArrayList<AlathkarButton> alathkarCardsCopy;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.alathkar_model_card);
        }

        public CardView getCard() {
            return card;
        }
    }

    public AlathkarAdapter(Context context, ArrayList<AlathkarButton> cards) {
        alathkarCards = new ArrayList<>(cards);
        alathkarCardsCopy = new ArrayList<>(cards);

        this.context = context;

        db = Room.databaseBuilder(context, AppDatabase.class, "HidayaDB").createFromAsset(
                "databases/HidayaDB.db").allowMainThreadQueries().build();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_alathkar, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        AlathkarButton card = alathkarCards.get(position);

        ((TextView) viewHolder.getCard().findViewById(R.id.namescreen))
                .setText(card.getName());
        viewHolder.getCard().setOnClickListener(card.getListener());

        int fav = card.getFavorite();
        if (fav == 0)
            ((ImageView) viewHolder.getCard().findViewById(R.id.athkar_fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star_outline));
        else if (fav == 1)
            ((ImageView) viewHolder.getCard().findViewById(R.id.athkar_fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star));

        viewHolder.getCard().setOnClickListener(card.getListener());
        viewHolder.getCard().findViewById(R.id.athkar_fav_btn).setOnClickListener(
                view -> {
                    if (card.getFavorite() == 0) {
                        db.athkarDao().setFav(card.getId(), 1);
                        card.setFavorite(1);
                    }
                    else if (card.getFavorite() == 1) {
                        db.athkarDao().setFav(card.getId(), 0);
                        card.setFavorite(0);
                    }
                    notifyItemChanged(position);

                    updateFavorites();
                });
    }

    public void filter(String text) {
        alathkarCards.clear();
        if (text.isEmpty())
            alathkarCards.addAll(alathkarCardsCopy);
        else {
            for(AlathkarButton alathkarButton: alathkarCardsCopy) {
                if(alathkarButton.getName().contains(text))
                    alathkarCards.add(alathkarButton);
            }
        }
        notifyDataSetChanged();
    }

    private void updateFavorites() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        Integer[] favAthkar = (Integer[]) db.athkarDao().getFavs().toArray();

        Gson gson = new Gson();
        String athkarJson = gson.toJson(favAthkar);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("favorite_athkar", athkarJson);
        editor.apply();
    }

    @Override
    public int getItemCount() {
        return alathkarCards.size();
    }
}

