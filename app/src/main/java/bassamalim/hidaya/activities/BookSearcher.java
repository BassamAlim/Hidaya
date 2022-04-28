package bassamalim.hidaya.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.BookSearcherAdapter;
import bassamalim.hidaya.databinding.ActivityBookSearcherBinding;
import bassamalim.hidaya.models.Book;
import bassamalim.hidaya.models.BookSearcherMatch;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.dialogs.FilterDialog;

public class BookSearcher extends AppCompatActivity {

    private ActivityBookSearcherBinding binding;
    private SharedPreferences pref;
    private Gson gson;
    private String[] bookTitles;
    private List<BookSearcherMatch> matches;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private BookSearcherAdapter adapter;
    private int maxMatches;
    private boolean[] selectedBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityBookSearcherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        init();

        setListeners();

        initFilterIb();

        setupSizeSpinner();
    }

    private void init() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        gson = new Gson();

        bookTitles = getResources().getStringArray(R.array.books_titles);

        searchView = binding.searchView;

        matches = new ArrayList<>();

        maxMatches = pref.getInt("books_searcher_matches_last_position", 10);
    }

    private void setListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                perform(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        binding.filterIb.setOnClickListener(v ->
                new FilterDialog<>(this, v, "اختر الكتب", bookTitles, selectedBooks,
                        adapter, binding.filterIb, "selected_search_books"));
    }

    private void setupSizeSpinner() {
        Spinner spinner = binding.sizeSpinner;
        int last = pref.getInt("books_searcher_matches_last_position", 0);
        spinner.setSelection(last);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long vId) {
                maxMatches = Integer.parseInt(spinner.getItemAtPosition(position).toString());

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("books_searcher_matches_last_position", position);
                editor.apply();

                if (adapter != null && adapter.getItemCount() > 0)
                    perform(searchView.getQuery().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void perform(String query) {
        selectedBooks = getSelectedBooks();
        search(query);

        if (matches.isEmpty()) {
            binding.notFoundTv.setVisibility(View.VISIBLE);
            binding.recycler.setVisibility(View.INVISIBLE);
        }
        else {
            binding.notFoundTv.setVisibility(View.INVISIBLE);
            binding.recycler.setVisibility(View.VISIBLE);

            setupRecycler(matches);

            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }

    private void search(String text) {
        matches.clear();

        String prefix = "/Books/";
        File dir = new File(getExternalFilesDir(null) + prefix);

        if (!dir.exists())
            return;

        File[] files = dir.listFiles();

        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            if (!selectedBooks[i])
                continue;

            String jsonStr = Utils.getJsonFromDownloads(getExternalFilesDir(null) +
                    prefix + i + ".json");
            Book book = gson.fromJson(jsonStr, Book.class);

            for (int j = 0; j < book.getChapters().length; j++) {
                Book.BookChapter chapter = book.getChapters()[j];

                for (int k = 0; k < chapter.getDoors().length; k++) {
                    Book.BookChapter.BookDoor door = chapter.getDoors()[k];
                    String doorText = door.getText();

                    Matcher m = Pattern.compile(text).matcher(doorText);
                    Spannable ss = new SpannableString(doorText);
                    while (m.find()) {
                        ss.setSpan(new ForegroundColorSpan(getColor(R.color.highlight_M)),
                                m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        matches.add(new BookSearcherMatch(
                                i, book.getBookInfo().getBookTitle(),
                                j, chapter.getChapterTitle(),
                                k, door.getDoorTitle(), ss));

                        if (matches.size() == maxMatches)
                            return;
                    }
                }
            }
        }
    }

    private void setupRecycler(List<BookSearcherMatch> matches) {
        recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BookSearcherAdapter(matches, searchView);
        recyclerView.setAdapter(adapter);
    }

    private void initFilterIb() {
        selectedBooks = getSelectedBooks();
        for (boolean bool : selectedBooks) {
            if (!bool) {
                binding.filterIb.setImageDrawable(
                        AppCompatResources.getDrawable(this, R.drawable.ic_filtered));
                break;
            }
        }
    }

    private boolean[] getSelectedBooks() {
        boolean[] defArr = new boolean[bookTitles.length];
        Arrays.fill(defArr, true);
        String defStr = gson.toJson(defArr);
        return gson.fromJson(pref.getString("selected_search_books", defStr), boolean[].class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if (recyclerView != null)
            recyclerView.setAdapter(null);
        adapter = null;
    }
}