package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;

import bassamalim.hidaya.activities.BookViewer;
import bassamalim.hidaya.adapters.BookChapterAdapter;
import bassamalim.hidaya.databinding.FragmentBookChaptersBinding;
import bassamalim.hidaya.enums.ListType;
import bassamalim.hidaya.models.Book;
import bassamalim.hidaya.models.BookChapterCard;
import bassamalim.hidaya.other.Utils;

public class BookChaptersFragment extends Fragment {

    private FragmentBookChaptersBinding binding;
    private RecyclerView recycler;
    private BookChapterAdapter adapter;
    private final ListType type;
    private final int bookId;
    private Book book;
    private boolean[] favs;

    public BookChaptersFragment(ListType type, int bookId) {
        this.type = type;
        this.bookId = bookId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBookChaptersBinding.inflate(inflater, container, false);

        setupRecycler();

        setSearchListeners();

        return binding.getRoot();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if (menuVisible) {
            adapter = new BookChapterAdapter(getContext(), makeCards(), bookId);
            recycler.setAdapter(adapter);
        }
    }

    private void getData() {
        String path = requireContext().getExternalFilesDir(null) + "/Books/" +
                bookId  + ".json";
        String jsonStr = Utils.getJsonFromDownloads(path);
        Gson gson = new Gson();
        book = gson.fromJson(jsonStr, Book.class);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String favsStr = pref.getString("book" + bookId + "_favs", "");
        if (favsStr.length() == 0)
            favs = new boolean[book.getChapters().length];
        else
            favs = gson.fromJson(favsStr, boolean[].class);
    }

    private ArrayList<BookChapterCard> makeCards() {
        getData();

        ArrayList<BookChapterCard> cards = new ArrayList<>();
        for (int i = 0; i < book.getChapters().length; i++) {
            if (type == ListType.All || (type == ListType.Favorite && favs[i])) {
                String chapterTitle = book.getChapters()[i].getChapterTitle();

                int finalI = i;
                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(getContext(), BookViewer.class);
                    intent.putExtra("book_id", bookId);
                    intent.putExtra("book_title", chapterTitle);
                    intent.putExtra("chapter_id", finalI);
                    startActivity(intent);
                };

                cards.add(new BookChapterCard(i, chapterTitle, favs[i], listener));
            }
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(layoutManager);
        adapter = new BookChapterAdapter(getContext(), makeCards(), bookId);
        recycler.setAdapter(adapter);
    }

    private void setSearchListeners() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}
