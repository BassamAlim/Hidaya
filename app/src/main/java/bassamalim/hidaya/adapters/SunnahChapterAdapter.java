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
import bassamalim.hidaya.models.SunnahChapterCard;

public class SunnahChapterAdapter extends RecyclerView.Adapter<SunnahChapterAdapter.ViewHolder> {

    private final Context context;
    private final SharedPreferences pref;
    private final Gson gson;
    private final int bookId;
    private final ArrayList<SunnahChapterCard> sunnahChaptersCards;
    private final ArrayList<SunnahChapterCard> sunnahChapterCardsCopy;
    private boolean[] favs;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final TextView tv;
        private final ImageButton favBtn;

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.sunnah_chapter_model_card);
            tv = view.findViewById(R.id.chapter_tv);
            favBtn = view.findViewById(R.id.fav_btn);
        }
    }

    public SunnahChapterAdapter(Context context, ArrayList<SunnahChapterCard> cards, int bookId) {
        this.context = context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        gson = new Gson();

        this.bookId = bookId;

        sunnahChaptersCards = new ArrayList<>(cards);
        sunnahChapterCardsCopy = new ArrayList<>(cards);

        getFavs();
    }

    private void getFavs() {
        String favsStr = pref.getString("book" + bookId + "_favs", "");
        if (favsStr.length() == 0)
            favs = new boolean[sunnahChaptersCards.size()];
        else
            favs = gson.fromJson(favsStr, boolean[].class);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_sunnah_chapter, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        SunnahChapterCard card = sunnahChaptersCards.get(position);

        viewHolder.tv.setText(sunnahChaptersCards.get(position).getChapterTitle());

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
                favs[sunnahChaptersCards.get(position).getChapterId()] = false;
                card.setFavorite(false);
            }
            else {
                favs[sunnahChaptersCards.get(position).getChapterId()] = true;
                card.setFavorite(true);
            }
            notifyItemChanged(position);
            updateFavorites();
        });
    }

    public void filter(String text) {
        sunnahChaptersCards.clear();
        if (text.isEmpty())
            sunnahChaptersCards.addAll(sunnahChapterCardsCopy);
        else {
            for(SunnahChapterCard chapterCard: sunnahChapterCardsCopy) {
                if (chapterCard.getChapterTitle().contains(text))
                    sunnahChaptersCards.add(chapterCard);
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
        return sunnahChaptersCards.size();
    }
}
