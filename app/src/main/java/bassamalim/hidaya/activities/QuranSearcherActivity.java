package bassamalim.hidaya.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.QuranSearcherAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AyatDB;
import bassamalim.hidaya.databinding.ActivityQuranSearcherBinding;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.other.Utils;

public class QuranSearcherActivity extends AppCompatActivity {

    private ActivityQuranSearcherBinding binding;
    private SharedPreferences pref;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private QuranSearcherAdapter adapter;
    private List<AyatDB> allAyat;
    private List<Ayah> matches;
    private int maxMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.myOnActivityCreated(this);
        binding = ActivityQuranSearcherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        init();

        setListeners();

        setupSizeSpinner();

        initRecycler();
    }

    private void init() {
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        searchView = binding.searchView;

        allAyat = db.ayahDao().getAll();

        matches = new ArrayList<>();

        maxMatches = pref.getInt("quran_searcher_matches_last_position", 1);
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
    }

    private void setupSizeSpinner() {
        Spinner spinner = binding.sizeSpinner;

        int last = pref.getInt("quran_searcher_matches_last_position", 0);
        spinner.setSelection(last);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long vId) {
                maxMatches = Integer.parseInt(spinner.getItemAtPosition(position).toString());

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("quran_searcher_matches_last_position", position);
                editor.apply();

                if (adapter != null && adapter.getItemCount() > 0)
                    perform(searchView.getQuery().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void perform(String query) {
        search(query);

        if (matches.isEmpty()) {
            binding.notFoundTv.setVisibility(View.VISIBLE);
            binding.recycler.setVisibility(View.INVISIBLE);
        }
        else {
            binding.notFoundTv.setVisibility(View.INVISIBLE);
            binding.recycler.setVisibility(View.VISIBLE);

            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }

    private void search(String text) {
        matches.clear();

        for (int i = 0; i < allAyat.size(); i++) {
            AyatDB a = allAyat.get(i);

            Matcher m = Pattern.compile(text).matcher(a.getAya_text_emlaey());
            SpannableString ss = new SpannableString(a.getAya_text_emlaey());
            while (m.find()) {
                ss.setSpan(new ForegroundColorSpan(getColor(R.color.highlight_M)),
                        m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                matches.add(new Ayah(a.getSura_no(), a.getSura_name_ar(), a.getPage(),
                        a.getAya_no(), a.getAya_tafseer(), ss));
            }

            if (matches.size() == maxMatches)
                break;
        }
    }

    private void initRecycler() {
        recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new QuranSearcherAdapter(this, matches);
        recyclerView.setAdapter(adapter);
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