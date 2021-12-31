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
import bassamalim.hidaya.adapters.RadioRecitersAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.TelawatDB;
import bassamalim.hidaya.database.TelawatRecitersDB;
import bassamalim.hidaya.databinding.ActivityRadioBinding;
import bassamalim.hidaya.models.ReciterCard;

public class RadioActivity extends AppCompatActivity {

    private ActivityRadioBinding binding;
    private RecyclerView recycler;
    private RadioRecitersAdapter adapter;
    private ArrayList<ReciterCard> cards;
    private List<TelawatDB> telawat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRadioBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        cards = makeCards();

        setupRecycler();

        setSearchListeners();
    }

    private ArrayList<ReciterCard> makeCards() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        List<TelawatRecitersDB> reciters = db.telawatRecitersDao().getAll();

        telawat = db.telawatDao().getAll();

        ArrayList<ReciterCard> cards = new ArrayList<>();
        for (int i = 0; i < reciters.size(); i++) {
            TelawatRecitersDB reciter = reciters.get(i);

            String name = reciter.getReciter_name();

            List<TelawatDB> versions = getVersions(reciter.getReciter_id());

            ReciterCard.RecitationVersion[] versionsArr =
                    new ReciterCard.RecitationVersion[versions.size()];

            for (int j = 0; j < versions.size(); j++) {
                TelawatDB telawa = versions.get(j);

                View.OnClickListener listener = v -> {
                    Intent intent = new Intent(v.getContext(), SurahsActivity.class);
                    intent.putExtra("reciter_id", telawa.getReciter_id());
                    intent.putExtra("rewaya", telawa.getRewaya());
                    startActivity(intent);
                };

                versionsArr[j] = new ReciterCard.RecitationVersion(telawa.getUrl(),
                        telawa.getRewaya(), telawa.getCount(), telawa.getSuras(), listener);
            }
            cards.add(new ReciterCard(name, versionsArr));
        }

        return cards;
    }

    private List<TelawatDB> getVersions(int id) {
        List<TelawatDB> result = new ArrayList<>();

        for (int i = 0; i < telawat.size(); i++) {
            TelawatDB telawa = telawat.get(i);

            if (telawa.getReciter_id() != id)
                continue;

            result.add(telawa);
        }
        return result;
    }

    private void setupRecycler() {
        recycler = findViewById(R.id.radio_reciters_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new RadioRecitersAdapter(cards);
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