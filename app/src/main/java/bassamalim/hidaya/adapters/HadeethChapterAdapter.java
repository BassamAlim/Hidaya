package bassamalim.hidaya.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.HadeethChapterCard;

public class HadeethChapterAdapter extends RecyclerView.Adapter<HadeethChapterAdapter.ViewHolder> {

    private final Context context;
    private final SharedPreferences pref;
    private final Gson gson;
    private final int bookId;
    private final ArrayList<HadeethChapterCard> hadeethChaptersCards;
    private final ArrayList<HadeethChapterCard> hadeethChapterCardsCopy;
    private boolean[] favs;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final TextView tv;
        private final ImageButton favBtn;

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.hadeeth_chapter_model_card);
            tv = view.findViewById(R.id.chapter_tv);
            favBtn = view.findViewById(R.id.fav_btn);
        }
    }

    public HadeethChapterAdapter(Context context, ArrayList<HadeethChapterCard> cards, int bookId) {
        this.context = context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();

        this.bookId = bookId;

        hadeethChaptersCards = new ArrayList<>(cards);
        hadeethChapterCardsCopy = new ArrayList<>(cards);

        getFavs();
    }

    private void getFavs() {
        String favsStr = pref.getString("book" + bookId + "_favs", "");
        if (favsStr.length() == 0)
            favs = new boolean[hadeethChaptersCards.size()];
        else
            favs = gson.fromJson(favsStr, boolean[].class);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_hadeeth_chapter, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        HadeethChapterCard card = hadeethChaptersCards.get(position);

        viewHolder.tv.setText(hadeethChaptersCards.get(position).getChapterTitle());

        boolean fav = card.getFavorite();
        if (fav)
            viewHolder.favBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star));
        else
            viewHolder.favBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_star_outline));


        viewHolder.card.setOnClickListener(card.getListener());

        viewHolder.favBtn.setOnClickListener(view -> {
            if (card.getFavorite()) {
                favs[hadeethChaptersCards.get(position).getChapterId()] = false;
                card.setFavorite(false);
            }
            else {
                favs[hadeethChaptersCards.get(position).getChapterId()] = true;
                card.setFavorite(true);
            }
            notifyItemChanged(position);
            updateFavorites();
        });
    }

    public void filter(String text) {
        hadeethChaptersCards.clear();
        if (text.isEmpty())
            hadeethChaptersCards.addAll(hadeethChapterCardsCopy);
        else {
            for(HadeethChapterCard chapterCard: hadeethChapterCardsCopy) {
                if (chapterCard.getChapterTitle().contains(text))
                    hadeethChaptersCards.add(chapterCard);
            }
        }
        notifyDataSetChanged();
    }

    private void updateFavorites() {
        String favStr = gson.toJson(favs);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("book" + bookId + "_favs", favStr);
        editor.apply();
    }

    @Override
    public int getItemCount() {
        return hadeethChaptersCards.size();
    }
}
