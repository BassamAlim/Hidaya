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
import java.util.Locale;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.TelawatSurahsAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.SuraDB;
import bassamalim.hidaya.databinding.ActivitySurahsBinding;
import bassamalim.hidaya.fragments.CollectionTelawatFragment;
import bassamalim.hidaya.models.ReciterSurahCard;

public class TelawatSurahsActivity extends AppCompatActivity {

    private ActivitySurahsBinding binding;
    private RecyclerView recycler;
    private TelawatSurahsAdapter adapter;
    private ArrayList<ReciterSurahCard> cards;
    private int reciterId;
    private int versionId;
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

        Intent intent = getIntent();
        reciterId = intent.getIntExtra("reciter_id", 0);
        versionId = intent.getIntExtra("version_id", 0);

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

        availableSurahs = db.telawatVersionsDao().getSuras(reciterId, versionId);
    }

    private ArrayList<ReciterSurahCard> makeCards() {
        ArrayList<ReciterSurahCard> cards = new ArrayList<>();
        for (int i = 0; i < 114; i++) {
            if (availableSurahs.contains("," + (i+1) + ",")) {
                String name = surahNames.get(i);
                String searchName = searchNames[i];

                int finalI = i;
                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(this, TelawatClient.class);
                    intent.setAction("start");

                    String rId = String.format(Locale.US, "%03d", reciterId);
                    String vId = String.format(Locale.US, "%02d", versionId);
                    String sId = String.format(Locale.US, "%03d", finalI);

                    String mediaId = rId + vId + sId;

                    intent.putExtra("media_id", mediaId);

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
        adapter = new TelawatSurahsAdapter(this, cards, reciterId, versionId);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isTaskRoot()) {
            Intent intent = new Intent(this, CollectionTelawatFragment.class);
            startActivity(intent);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}