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

import bassamalim.hidaya.activities.SunnahViewer;
import bassamalim.hidaya.adapters.SunnahChapterAdapter;
import bassamalim.hidaya.databinding.FragmentSunnahChaptersBinding;
import bassamalim.hidaya.enums.ListType;
import bassamalim.hidaya.models.SunnahBook;
import bassamalim.hidaya.models.SunnahChapterCard;
import bassamalim.hidaya.other.Utils;

public class SunnahChaptersFragment extends Fragment {

    private FragmentSunnahChaptersBinding binding;
    private RecyclerView recycler;
    private SunnahChapterAdapter adapter;
    private final ListType type;
    private final int bookId;
    private SunnahBook book;
    private boolean[] favs;

    public SunnahChaptersFragment(ListType type, int bookId) {
        this.type = type;
        this.bookId = bookId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSunnahChaptersBinding.inflate(inflater, container, false);

        setupRecycler();

        setSearchListeners();

        return binding.getRoot();
    }

    private void getData() {
        String path = requireContext().getExternalFilesDir(null) + "/Sunnah Downloads/" +
                bookId  + ".json";
        String jsonStr = Utils.getJsonFromDownloads(path);
        Gson gson = new Gson();
        book = gson.fromJson(jsonStr, SunnahBook.class);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String favsStr = pref.getString("book" + bookId + "_favs", "");
        if (favsStr.length() == 0)
            favs = new boolean[book.getChapters().length];
        else
            favs = gson.fromJson(favsStr, boolean[].class);
    }

    private ArrayList<SunnahChapterCard> makeCards() {
        getData();

        ArrayList<SunnahChapterCard> cards = new ArrayList<>();
        for (int i = 0; i < book.getChapters().length; i++) {
            if (type == ListType.All || (type == ListType.Favorite && favs[i])) {
                String chapterTitle = book.getChapters()[i].getChapterTitle();

                int finalI = i;
                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(getContext(), SunnahViewer.class);
                    intent.putExtra("book_id", bookId);
                    intent.putExtra("book_title", chapterTitle);
                    intent.putExtra("chapter_id", finalI);
                    startActivity(intent);
                };

                cards.add(new SunnahChapterCard(i, chapterTitle, favs[i], listener));
            }
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(layoutManager);
        adapter = new SunnahChapterAdapter(getContext(), makeCards(), bookId);
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
