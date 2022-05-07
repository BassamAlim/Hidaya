package bassamalim.hidaya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.models.BookSearcherMatch;
import bassamalim.hidaya.replacements.FilteredRecyclerAdapter;

public class BookSearcherAdapter
        extends FilteredRecyclerAdapter<BookSearcherAdapter.ViewHolder> {

    private final List<BookSearcherMatch> items;
    private final SearchView searchView;

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

    public BookSearcherAdapter(List<BookSearcherMatch> items, SearchView searchView) {
        this.items = items;
        this.searchView = searchView;
    }

    @NonNull @Override
    public BookSearcherAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup, int viewType) {

        return new BookSearcherAdapter.ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_book_searcher, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(BookSearcherAdapter.ViewHolder viewHolder, final int position) {
        BookSearcherMatch card = items.get(position);

        viewHolder.bookTitleTv.setText(card.getBookTitle());
        viewHolder.chapterTitleTv.setText(card.getChapterTitle());
        viewHolder.doorTitleTv.setText(card.getDoorTitle());
        viewHolder.textTv.setText(card.getText());
    }

    @Override
    public void filter(String text, boolean[] selected) {
        searchView.setQuery(searchView.getQuery(), true);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
