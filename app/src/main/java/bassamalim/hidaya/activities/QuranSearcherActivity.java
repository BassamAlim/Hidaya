package bassamalim.hidaya.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import bassamalim.hidaya.adapters.QuranSearcherAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AyatDB;
import bassamalim.hidaya.databinding.ActivityQuranSearcherBinding;
import bassamalim.hidaya.other.Utils;

public class QuranSearcherActivity extends AppCompatActivity {

    private ActivityQuranSearcherBinding binding;
    private SharedPreferences pref;
    private RecyclerView recyclerView;
    private QuranSearcherAdapter adapter;
    private List<AyatDB> allAyat;
    private List<AyatDB> matches;
    private int maxMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityQuranSearcherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initiate();

        initSpinner();

        initRecycler();

        setListeners();
    }

    private void initiate() {
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        allAyat = db.ayahDao().getAll();

        matches = new ArrayList<>();

        maxMatches = pref.getInt("quran_searcher_matches_last_position", 1);
    }

    private void initRecycler() {
        recyclerView = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new QuranSearcherAdapter(matches);
        recyclerView.setAdapter(adapter);
    }

    private void setListeners() {
        binding.searchBtn.setOnClickListener(v -> perform());
    }

    private void perform() {
        search(binding.editText.getText().toString());

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

        int counter = 0;
        for (int i = 0; i < allAyat.size(); i++) {
            if (allAyat.get(i).getAya_text_emlaey().contains(text)) {
                matches.add(allAyat.get(i));
                counter++;
            }
            if (counter == maxMatches)
                break;
        }
    }

    private void initSpinner() {
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