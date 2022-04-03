package bassamalim.hidaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.HadeethSearcherMatch;

public class HadeethSearcherAdapter extends RecyclerView.Adapter<HadeethSearcherAdapter.ViewHolder>{

    private final List<HadeethSearcherMatch> hadeethCards;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookTitleTv;
        private final TextView chapterTitleTv;
        private final TextView doorTitleTv;
        private final TextView textTv;

        public ViewHolder(View view) {
            super(view);
            bookTitleTv = view.findViewById(R.id.book_title_tv);
            chapterTitleTv = view.findViewById(R.id.chapter_title_tv);
            doorTitleTv = view.findViewById(R.id.door_title_tv);
            textTv = view.findViewById(R.id.text_tv);
        }
    }

    public HadeethSearcherAdapter(List<HadeethSearcherMatch> hadeethCards) {
        this.hadeethCards = hadeethCards;
    }

    @NonNull @Override
    public HadeethSearcherAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int viewType) {

        return new HadeethSearcherAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_hadeeth_searcher, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(HadeethSearcherAdapter.ViewHolder viewHolder, final int position) {
        HadeethSearcherMatch card = hadeethCards.get(position);

        viewHolder.bookTitleTv.setText(card.getBookTitle());
        viewHolder.chapterTitleTv.setText(card.getChapterTitle());
        viewHolder.doorTitleTv.setText(card.getDoorTitle());
        viewHolder.textTv.setText(card.getText());
    }

    @Override
    public int getItemCount() {
        return hadeethCards.size();
    }
}
