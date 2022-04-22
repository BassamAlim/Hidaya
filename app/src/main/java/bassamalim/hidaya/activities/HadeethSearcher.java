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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.HadeethSearcherAdapter;
import bassamalim.hidaya.databinding.ActivityHadeethSearcherBinding;
import bassamalim.hidaya.models.HadeethBook;
import bassamalim.hidaya.models.HadeethSearcherMatch;
import bassamalim.hidaya.other.Utils;

public class HadeethSearcher extends AppCompatActivity {

    private ActivityHadeethSearcherBinding binding;
    private SharedPreferences pref;
    private Gson gson;
    private RecyclerView recyclerView;
    private HadeethSearcherAdapter adapter;
    private int maxMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityHadeethSearcherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        initiate();

        initSpinner();

        setListeners();
    }

    private void initiate() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        gson = new Gson();

        maxMatches = pref.getInt("hadeeth_searcher_matches_last_position", 10);
    }

    private void initRecycler(List<HadeethSearcherMatch> matches) {
        recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HadeethSearcherAdapter(matches);
        recyclerView.setAdapter(adapter);
    }

    private void setListeners() {
        binding.searchBtn.setOnClickListener(v -> perform());
    }

    private void perform() {
        List<HadeethSearcherMatch> matches = search(binding.editText.getText().toString());

        if (matches.isEmpty()) {
            binding.notFoundTv.setVisibility(View.VISIBLE);
            binding.recycler.setVisibility(View.INVISIBLE);
        }
        else {
            binding.notFoundTv.setVisibility(View.INVISIBLE);
            binding.recycler.setVisibility(View.VISIBLE);

            initRecycler(matches);

            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }

    private List<HadeethSearcherMatch> search(String text) {
        List<HadeethSearcherMatch> matches = new ArrayList<>();

        String prefix = "/Hadeeth Downloads/";
        File dir = new File(getExternalFilesDir(null) + prefix);

        if (!dir.exists())
            return matches;

        File[] files = dir.listFiles();

        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            String jsonStr = Utils.getJsonFromDownloads(getExternalFilesDir(null) +
                    prefix + i + ".json");
            HadeethBook book = gson.fromJson(jsonStr, HadeethBook.class);

            for (int j = 0; j < book.getChapters().length; j++) {
                HadeethBook.BookChapter chapter = book.getChapters()[j];

                for (int k = 0; k < chapter.getDoors().length; k++) {
                    HadeethBook.BookChapter.BookDoor door = chapter.getDoors()[k];

                    String doorText = door.getText();
                    int index = doorText.indexOf(text);

                    if (index != -1) {
                        Spannable ss = new SpannableString(doorText);
                        ss.setSpan(new ForegroundColorSpan(getColor(R.color.highlight_M)), index,
                                index + text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        matches.add(new HadeethSearcherMatch(
                                i, book.getBookInfo().getBookTitle(),
                                j, chapter.getChapterTitle(),
                                k, door.getDoorTitle(), ss));
                    }

                    if (matches.size() == maxMatches)
                        return matches;
                }
            }
        }

        return matches;
    }

    private void initSpinner() {
        Spinner spinner = binding.sizeSpinner;

        int last = pref.getInt("hadeeth_searcher_matches_last_position", 0);
        spinner.setSelection(last);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long vId) {
                maxMatches = Integer.parseInt(spinner.getItemAtPosition(position).toString());

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("hadeeth_searcher_matches_last_position", position);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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