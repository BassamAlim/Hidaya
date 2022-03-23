package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.adapters.AthkarViewerAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.ThikrsDB;
import bassamalim.hidaya.databinding.ActivityAlathkarViewerBinding;
import bassamalim.hidaya.models.ThikrCard;
import bassamalim.hidaya.other.Utils;

public class AthkarViewer extends AppCompatActivity {

    private ActivityAlathkarViewerBinding binding;
    private AppDatabase db;
    private List<ThikrsDB> thikrs;
    private RecyclerView recycler;
    private AthkarViewerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityAlathkarViewerBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        Intent intent = getIntent();
        int id = intent.getIntExtra("thikr_id", 0);
        binding.topBarTitle.setText(db.athkarDao().getName(id));

        thikrs = getThikrs(id);

        setupRecycler();
    }

    private List<ThikrsDB> getThikrs(int id) {
        return db.thikrsDao().getThikrs(id);
    }

    private ArrayList<ThikrCard> makeCards() {
        ArrayList<ThikrCard> cards = new ArrayList<>();
        for (int i = 0; i < thikrs.size(); i++) {
            ThikrsDB t = thikrs.get(i);
            cards.add(new ThikrCard(t.getThikr_id(), t.getTitle(), t.getText(), t.getFadl(),
                    t.getReference(), t.getRepetition()));
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new AthkarViewerAdapter(this, makeCards());
        recycler.setAdapter(adapter);
    }

    public void onDestroy() {
        super.onDestroy();
        binding = null;
        recycler.setAdapter(null);
        adapter = null;
    }
}