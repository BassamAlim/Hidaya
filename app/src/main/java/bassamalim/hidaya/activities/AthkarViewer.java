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
import bassamalim.hidaya.models.Thikr;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;

public class AthkarViewer extends AppCompatActivity {

    private ActivityAthkarViewerBinding binding;
    private SharedPreferences pref;
    private AppDatabase db;
    private RecyclerView recycler;
    private SeekBar textSizeSb;
    private AthkarViewerAdapter adapter;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        language = Utils.onActivityCreateSetLocale(this);
        binding = ActivityAthkarViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        Intent intent = getIntent();
        int id = intent.getIntExtra("thikr_id", 0);

        String topBarTitle = language.equals("en") ? db.athkarDao().getNameEn(id) :
                db.athkarDao().getName(id);
        binding.topBarTitle.setText(topBarTitle);

        setupRecycler(id);

        setupListeners();
    }

    private List<ThikrsDB> getThikrs(int id) {
        return db.thikrsDao().getThikrs(id);
    }

    private ArrayList<Thikr> makeCards(List<ThikrsDB> thikrs) {
        ArrayList<Thikr> cards = new ArrayList<>();
        for (int i = 0; i < thikrs.size(); i++) {
            ThikrsDB t = thikrs.get(i);

            if (language.equals("en") && (t.getText_en() == null || t.getText_en().length() < 1))
                continue;

            if (language.equals("en"))
                cards.add(new Thikr(t.getThikr_id(), t.getTitle_en(), t.getText_en(),
                        t.getText_en_translation(), t.getFadl_en(), t.getReference_en(),
                        t.getRepetition_en(), v -> new InfoDialog(getString(R.string.reference), t.getReference_en())
                            .show(getSupportFragmentManager(), InfoDialog.TAG)));
            else
                cards.add(new Thikr(t.getThikr_id(), t.getTitle(), t.getText(),
                        t.getText_en_translation(), t.getFadl(), t.getReference(),
                        t.getRepetition(), v -> new InfoDialog(getString(R.string.reference),
                        t.getReference()).show(getSupportFragmentManager(), InfoDialog.TAG)));
        }
        return cards;
    }

    private void setupRecycler(int id) {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new AthkarViewerAdapter(this, makeCards(getThikrs(id)), language);
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
