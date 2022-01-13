package bassamalim.hidaya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.models.ReciterCard;

public class TelawatAdapter extends RecyclerView.Adapter<TelawatAdapter.ViewHolder> {

    private final Context context;
    private final AppDatabase db;
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private final ArrayList<ReciterCard> recitersCards;
    private final ArrayList<ReciterCard> recitersCardsCopy;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final RecyclerView recyclerView;

        public ViewHolder(View view) {
            super(view);

            card = view.findViewById(R.id.reciter_model_card);
            recyclerView = view.findViewById(R.id.versions_recycler);
        }
    }

    public TelawatAdapter(Context context, ArrayList<ReciterCard> cards) {
        this.context = context;

        db = Room.databaseBuilder(context, AppDatabase.class, "HidayaDB").createFromAsset(
                "databases/HidayaDB.db").allowMainThreadQueries().build();

        recitersCards = new ArrayList<>(cards);
        recitersCardsCopy = new ArrayList<>(cards);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_radio_reciter, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ReciterCard card = recitersCards.get(position);
        View cardView = viewHolder.card;

        ((TextView) cardView.findViewById(R.id.reciter_namescreen)).setText(card.getName());

        int fav = card.getFavorite();
        if (fav == 0)
            ((ImageView) viewHolder.card.findViewById(R.id.telawa_fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star_outline));
        else if (fav == 1)
            ((ImageView) viewHolder.card.findViewById(R.id.telawa_fav_btn)).setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star));

        viewHolder.card.findViewById(R.id.telawa_fav_btn).setOnClickListener(
                view -> {
                    if (card.getFavorite() == 0) {
                        db.telawatRecitersDao().setFav(card.getId(), 1);
                        card.setFavorite(1);
                    }
                    else if (card.getFavorite() == 1) {
                        db.telawatRecitersDao().setFav(card.getId(), 0);
                        card.setFavorite(0);
                    }
                    notifyItemChanged(position);
                });

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setInitialPrefetchItemCount(card.getVersions().size());
        TelawaVersionAdapter versionsAdapter = new TelawaVersionAdapter(card.getVersions());
        viewHolder.recyclerView.setLayoutManager(layoutManager);
        viewHolder.recyclerView.setAdapter(versionsAdapter);
        viewHolder.recyclerView.setRecycledViewPool(viewPool);
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


class TelawaVersionAdapter extends RecyclerView.Adapter<TelawaVersionAdapter.ViewHolder> {

    private final List<ReciterCard.RecitationVersion> versions;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tv;
        private final ImageButton download_btn;

        public ViewHolder(View view) {
            super(view);

            cardView = view.findViewById(R.id.telawa_version_model_card);
            tv = view.findViewById(R.id.version_namescreen);
            download_btn = view.findViewById(R.id.download_btn);
        }
    }

    public TelawaVersionAdapter(List<ReciterCard.RecitationVersion> versions) {
        this.versions = versions;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_telawa_version, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ReciterCard.RecitationVersion ver = versions.get(position);

        viewHolder.tv.setText(ver.getRewaya());

        viewHolder.cardView.setOnClickListener(ver.getListener());
    }

    @Override
    public int getItemCount() {
        return versions.size();
    }
}
