package bassamalim.hidaya.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.AthkarViewerAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.ThikrsDB;
import bassamalim.hidaya.databinding.ActivityAthkarViewerBinding;
import bassamalim.hidaya.dialogs.InfoDialog;
import bassamalim.hidaya.models.ThikrCard;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;

public class AthkarViewer extends AppCompatActivity {

    private ActivityAthkarViewerBinding binding;
    private SharedPreferences pref;
    private AppDatabase db;
    private List<ThikrsDB> thikrs;
    private RecyclerView recycler;
    private SeekBar textSizeSb;
    private AthkarViewerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.myOnActivityCreated(this, this);
        binding = ActivityAthkarViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        Intent intent = getIntent();
        int id = intent.getIntExtra("thikr_id", 0);
        binding.topBarTitle.setText(db.athkarDao().getName(id));

        thikrs = getThikrs(id);

        setupRecycler();

        setupListeners();
    }

    private List<ThikrsDB> getThikrs(int id) {
        return db.thikrsDao().getThikrs(id);
    }

    private ArrayList<ThikrCard> makeCards() {
        ArrayList<ThikrCard> cards = new ArrayList<>();
        for (int i = 0; i < thikrs.size(); i++) {
            ThikrsDB t = thikrs.get(i);
            cards.add(new ThikrCard(t.getThikr_id(), t.getTitle(), t.getText(), t.getFadl(),
                    t.getReference(), t.getRepetition(), v ->
                    new InfoDialog(getString(R.string.reference), t.getReference()).show(
                            getSupportFragmentManager(), InfoDialog.TAG)));
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

    private void setupListeners() {
        textSizeSb = binding.textSizeSb;

        textSizeSb.setProgress(pref.getInt(getString(R.string.alathkar_text_size_key), 15));

        binding.textSizeIb.setOnClickListener(v -> {
            if (textSizeSb.getVisibility() == View.GONE)
                textSizeSb.setVisibility(View.VISIBLE);
            else
                textSizeSb.setVisibility(View.GONE);
        });

        textSizeSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(Global.TAG, String.valueOf(seekBar.getProgress()));

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(getString(R.string.alathkar_text_size_key), seekBar.getProgress());
                editor.apply();

                adapter.setTextSize(seekBar.getProgress());
                recycler.setAdapter(null);
                recycler.setAdapter(adapter);
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
