package bassamalim.hidaya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.models.ReciterCard;

import java.util.ArrayList;

public class RadioRecitersAdapter extends RecyclerView.Adapter<RadioRecitersAdapter.ViewHolder> {

    private final Context context;
    private final AppDatabase db;
    private final ArrayList<ReciterCard> recitersCards;
    private final ArrayList<ReciterCard> recitersCardsCopy;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;

        public ViewHolder(View view) {
            super(view);

            card = view.findViewById(R.id.reciter_model_card);
        }

        public CardView getCard() {
            return card;
        }
    }

    public RadioRecitersAdapter(Context context, ArrayList<ReciterCard> cards) {
        recitersCards = new ArrayList<>(cards);
        recitersCardsCopy = new ArrayList<>(cards);

        this.context = context;

        db = Room.databaseBuilder(context, AppDatabase.class, "HidayaDB").createFromAsset(
                "databases/HidayaDB.db").allowMainThreadQueries().build();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_radio_reciter, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ReciterCard card = recitersCards.get(position);
        View cardView = viewHolder.getCard();

        ((TextView) cardView.findViewById(R.id.reciter_namescreen)).setText(card.getName());



        int fav = card.getFavorite();
        if (fav == 0)
            ((ImageView) viewHolder.getCard().findViewById(R.id.telawa_fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star_outline));
        else if (fav == 1)
            ((ImageView) viewHolder.getCard().findViewById(R.id.telawa_fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star));

        viewHolder.getCard().findViewById(R.id.telawa_fav_btn).setOnClickListener(
                view -> {
                    if (card.getFavorite() == 0) {
                        db.telawatRecitersDao().setFav(position, 1);
                        card.setFavorite(1);
                    }
                    else if (card.getFavorite() == 1) {
                        db.telawatRecitersDao().setFav(position, 0);
                        card.setFavorite(0);
                    }
                    notifyItemChanged(position);
                });

        Button[] buttons = new Button[5];
        buttons[0] = cardView.findViewById(R.id.version1);
        buttons[1] = cardView.findViewById(R.id.version2);
        buttons[2] = cardView.findViewById(R.id.version3);
        buttons[3] = cardView.findViewById(R.id.version4);
        buttons[4] = cardView.findViewById(R.id.version5);

        int length = card.getVersions().length;
        for (int i = 0; i < 5; i++) {
            if (i < length) {
                buttons[i].setVisibility(View.VISIBLE);
                buttons[i].setText(card.getVersions()[i].getRewaya());
                buttons[i].setOnClickListener(card.getVersions()[i].getListener());
            }
            else
                buttons[i].setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recitersCards.size();
    }

    public void filter(String text) {
        recitersCards.clear();
        if (text.isEmpty())
            recitersCards.addAll(recitersCardsCopy);
        else {
            for(ReciterCard reciterCard: recitersCardsCopy) {
                if (reciterCard.getName().contains(text))
                    recitersCards.add(reciterCard);
            }
        }
        notifyDataSetChanged();
    }

}
