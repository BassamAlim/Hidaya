package bassamalim.hidaya.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.BookViewerAdapter;
import bassamalim.hidaya.databinding.ActivityBookViewerBinding;
import bassamalim.hidaya.models.Book;
import bassamalim.hidaya.models.BookDoorCard;
import bassamalim.hidaya.other.Utils;

public class BookViewer extends AppCompatActivity {

    private ActivityBookViewerBinding binding;
    private SharedPreferences pref;
    private int bookId;
    private int chapterId;
    private Book.BookChapter.BookDoor[] doors;
    private boolean[] favs;
    private RecyclerView recycler;
    private SeekBar textSizeSb;
    private BookViewerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityBookViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        bookId = intent.getIntExtra("book_id", 0);
        chapterId = intent.getIntExtra("chapter_id", 0);
        binding.topBarTitle.setText(intent.getStringExtra("book_title"));

        getData();

        setupRecycler();

        setupListeners();
    }

    private void getData() {
        String path = getExternalFilesDir(null) + "/Books/" + bookId  + ".json";
        String jsonStr = Utils.getJsonFromDownloads(path);
        Gson gson = new Gson();
        Book book = gson.fromJson(jsonStr, Book.class);
        doors = book.getChapters()[chapterId].getDoors();

        String favsStr = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("book" + bookId + "_chapter" + chapterId + "_favs", "");
        if (favsStr.length() == 0)
            favs = new boolean[doors.length];
        else
            favs = gson.fromJson(favsStr, boolean[].class);
    }

    private ArrayList<BookDoorCard> makeCards() {
        ArrayList<BookDoorCard> cards = new ArrayList<>();
        for (int i = 0; i < doors.length; i++) {
            Book.BookChapter.BookDoor door = doors[i];
            cards.add(new BookDoorCard(door.getDoorId(), door.getDoorTitle(),
                    door.getText(), favs[i]));
        }
        return cards;
    }

    private void setupRecycler() {
        recycler = binding.recycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        adapter = new BookViewerAdapter(this, makeCards(), bookId, chapterId);
        recycler.setAdapter(adapter);
    }

    private void setupListeners() {
        textSizeSb = binding.textSizeSb;

        textSizeSb.setProgress(pref.getInt(getString(R.string.books_text_size_key), 15));

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
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(getString(R.string.books_text_size_key), seekBar.getProgress());
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