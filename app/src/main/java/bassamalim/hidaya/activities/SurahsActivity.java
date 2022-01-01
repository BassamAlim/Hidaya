package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.ReciterSurahsAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.SuraDB;
import bassamalim.hidaya.databinding.ActivitySurahsBinding;
import bassamalim.hidaya.models.ReciterSurahCard;

public class SurahsActivity extends AppCompatActivity {

    private ActivitySurahsBinding binding;
    private RecyclerView recycler;
    private ReciterSurahsAdapter adapter;
    private ArrayList<ReciterSurahCard> cards;
    private int reciterId;
    private String rewaya;
    private String availableSurahs;
    private ArrayList<String> surahNames;
    private String[] searchNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySurahsBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        reciterId = getIntent().getIntExtra("reciter_id", 0);
        rewaya = getIntent().getStringExtra("rewaya");

        getData();

        cards = makeCards();

        setupRecycler();

        setSearchListeners();
    }

    private void getData() {
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();
        List<SuraDB> suras = db.suraDao().getAll();

        surahNames = new ArrayList<>();
        searchNames = new String[114];
        for (int i = 0; i < 114; i++) {
            surahNames.add(suras.get(i).getSura_name());
            searchNames[i] = suras.get(i).getSearch_name();
        }

        availableSurahs = db.telawatVersionsDao().getSuras(reciterId, rewaya);
    }

    private ArrayList<ReciterSurahCard> makeCards() {
        ArrayList<ReciterSurahCard> cards = new ArrayList<>();
        for (int i = 0; i < 114; i++) {
            if (availableSurahs.contains("," + (i+1) + ",")) {
                String name = surahNames.get(i);
                String searchName = searchNames[i];
                int finalI = i;
                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(this, RadioClient.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.setAction("start");
                    intent.putExtra("reciter_id", reciterId);
                    intent.putExtra("rewaya", rewaya);
                    intent.putExtra("surah_id", finalI);
                    intent.putStringArrayListExtra("surah_names", surahNames);
                    startActivity(intent);
                };
                cards.add(new ReciterSurahCard(i, name, searchName, listener));
            }
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = findViewById(R.id.reciter_surahs_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new ReciterSurahsAdapter(cards);
        recycler.setAdapter(adapter);
    }

    private void setSearchListeners() {
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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