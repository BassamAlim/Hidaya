package bassamalim.hidaya.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AyatDB;
import bassamalim.hidaya.databinding.ActivityQuranBinding;
import bassamalim.hidaya.enums.States;
import bassamalim.hidaya.helpers.RecitationManager;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.other.Util;
import bassamalim.hidaya.popups.QuranSettingsPopup;
import bassamalim.hidaya.popups.TafseerPopup;
import bassamalim.hidaya.replacements.DoubleClickLMM;
import bassamalim.hidaya.replacements.DoubleClickableSpan;
import bassamalim.hidaya.replacements.SwipeActivity;

public class QuranActivity extends SwipeActivity {

    private final int QURAN_PAGES = 604;
    private ActivityQuranBinding binding;
    private AppDatabase db;
    private SharedPreferences pref;
    private String action;
    private LinearLayout mainLinear;
    private int surahIndex;
    private int currentPage;
    private String currentPageText;
    private String currentSurah;
    private int textSize;
    private ArrayList<Ayah> allAyahs;
    private ArrayList<Ayah> arr;
    private TextView target;
    private boolean scrolled;
    private Ayah selected;
    private RecitationManager rcMgr;
    private List<AyatDB> ayatDB;
    private String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeify();
        binding = ActivityQuranBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.infoBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        initiate();

        setListeners();

        Intent intent = getIntent();
        action = intent.getAction();

        setupManager();

        action(intent);

        buildPage(currentPage);
    }

    private void themeify() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        textSize = pref.getInt(getString(R.string.quran_text_size_key), 30);
        theme = pref.getString(getString(R.string.theme_key), "ThemeM");

        switch (theme) {
            case "ThemeM":
                setTheme(R.style.QuranM);
                break;
            case "ThemeL":
                setTheme(R.style.QuranL);
                break;
        }
    }

    private void initiate() {
        mainLinear = binding.mainLinear;

        db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();
        ayatDB = db.ayahDao().getAll();
    }

    private void action(Intent intent) {
        switch (action) {
            case "by_surah":
                surahIndex = intent.getIntExtra("surah_id", 0);
                setCurrentPage(getPage(surahIndex));
                break;
            case "by_page":
                setCurrentPage(intent.getIntExtra("page", 0));
                break;
            case "random":
                setCurrentPage(new Random().nextInt(QURAN_PAGES - 1));
                break;
        }
    }

    private void setListeners() {
        binding.bookmarkButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            String text = currentPageText + ", " + currentSurah;
            editor.putInt("bookmarked_page", currentPage);
            editor.putString("bookmarked_text", text);
            editor.apply();
            Toast.makeText(this, "تم حفظ الصفحة", Toast.LENGTH_SHORT).show();
        });
        binding.prevAyah.setOnClickListener(view -> rcMgr.prevAyah());
        binding.play.setOnClickListener(view -> {
            if (rcMgr.getState() == States.Playing) {
                if (rcMgr.getLastPlayed() != null)
                    selected = rcMgr.getLastPlayed();

                rcMgr.pause();
                updateUi(States.Paused);
            }
            else if (rcMgr.getState() == States.Paused) {
                if (selected == null)
                    rcMgr.resume();
                else {
                    rcMgr.setChosenSurah(selected.getSurah());
                    rcMgr.requestPlay(selected);
                }
                updateUi(States.Playing);
            }
            else {
                if (selected == null)
                    selected = allAyahs.get(0);

                rcMgr.setChosenSurah(selected.getSurah());
                rcMgr.requestPlay(selected);

                updateUi(States.Playing);
            }
            selected = null;
        });
        binding.nextAyah.setOnClickListener(view -> rcMgr.nextAyah());
        binding.recitationSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuranSettingsPopup.class);
            settingsPopup.launch(intent);
        });
    }

    ActivityResultLauncher<Intent> settingsPopup = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    textSize = data.getIntExtra("text_size", 30);
                    buildPage(currentPage);
                }
            });

    private void setupManager() {
        rcMgr = new RecitationManager(this);

        RecitationManager.Coordinator uiListener = new RecitationManager.Coordinator() {
            @Override
            public void onUiUpdate(States state) {
                updateUi(state);
            }

            @Override
            public Ayah getAyah(int index) {
                return allAyahs.get(index);
            }

            @Override
            public void nextPage() {
                next();
            }
        };
        rcMgr.setCoordinator(uiListener);
    }

    @Override
    protected void previous() {
        if (currentPage > 1) {
            setCurrentPage(--currentPage);
            buildPage(currentPage);
            binding.scrollView.scrollTo(0, 0);
        }
    }

    @Override
    protected void next() {
        if (currentPage < QURAN_PAGES) {
            setCurrentPage(++currentPage);
            buildPage(currentPage);
            binding.scrollView.scrollTo(0, 0);
        }
    }

    private int getPage(int surahIndex) {
        return db.suraDao().getPage(surahIndex);
    }

    private int getPageStart(int pageNumber) {
        int start;
        int counter = 0;
        while (ayatDB.get(counter).getPage() < pageNumber)
            counter++;
        start = counter;

        return start;
    }

    private void buildPage(int pageNumber) {
        mainLinear.removeAllViews();
        allAyahs = new ArrayList<>();
        arr = new ArrayList<>();
        ArrayList<ArrayList<Ayah>> pageAyahs = new ArrayList<>();

        int counter = getPageStart(pageNumber);
        do {
            AyatDB ayah = ayatDB.get(counter);
            int surahNum = ayah.getSura_no();    // starts from 1
            int ayahNum = ayah.getAya_no();

            Ayah ayahModel = new Ayah(ayah.getJozz(), surahNum, ayahNum, ayah.getSura_name_ar(),
                    ayah.getAya_text() + " ", ayah.getAya_tafseer());

            if (ayahNum == 1) {
                if (arr.size() > 0) {
                    pageAyahs.add(arr);
                    publish(arr);
                }
                addHeader(surahNum, ayahModel.getSurahName());
            }
            arr.add(ayahModel);

        } while (++counter != 6236 && ayatDB.get(counter).getPage() == pageNumber);

        int juz = arr.get(0).getJuz();

        pageAyahs.add(arr);
        publish(arr);

        finalize(juz, pageAyahs.get(findMainSurah(pageAyahs)).get(0).getSurahName());
    }

    private void publish(ArrayList<Ayah> list) {
        TextView screen = screen();
        StringBuilder text = new StringBuilder();

        for (int i=0; i<list.size(); i++) {
            list.get(i).setStart(text.length());
            text.append(list.get(i).getText());
            list.get(i).setEnd(text.length()-3);
        }

        SpannableString ss = new SpannableString(text);
        for (int i = 0; i < list.size(); i++) {
            int finalI = i;
            DoubleClickableSpan clickableSpan = new DoubleClickableSpan() {
                @Override
                public void onDoubleClick(View textView) {
                    new TafseerPopup(list.get(finalI).getTafseer()).show(
                            getSupportFragmentManager(), TafseerPopup.TAG);
                }
                @Override
                public void onClick(@NonNull View widget) {
                    selected = allAyahs.get(list.get(finalI).getIndex());
                }
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            list.get(finalI).setSS(ss);
            ss.setSpan(clickableSpan, list.get(i).getStart(), list.get(i).getEnd(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            list.get(finalI).setIndex(allAyahs.size());
            list.get(finalI).setScreen(screen);
            allAyahs.add(list.get(finalI));
        }
        rcMgr.setAllAyahsSize(allAyahs.size());

        screen.setText(ss);
        if (theme.equals("ThemeM"))
            screen.setMovementMethod(DoubleClickLMM.getInstance(
                    getResources().getColor(R.color.highlight_M)));
        else
            screen.setMovementMethod(DoubleClickLMM.getInstance(
                    getResources().getColor(R.color.highlight_L)));
        mainLinear.addView(screen);
        arr = new ArrayList<>();
    }

    private void finalize(int juz, String name) {
        String juzText = "جزء " + Util.translateNumbers(String.valueOf(juz));
        currentSurah = "سُورَة " + name;
        currentPageText = "صفحة " + Util.translateNumbers(String.valueOf(currentPage));
        binding.juzNumber.setText(juzText);
        binding.suraName.setText(currentSurah);
        binding.pageNumber.setText(currentPageText);

        ScrollView scroll = binding.scrollView;
        if (action.equals("by_surah") && !scrolled) {
            long delay = 100; //delay to let finish with possible modifications to ScrollView
            scroll.postDelayed(() -> scroll.smoothScrollTo(0, target.getTop()), delay);
            scrolled = true;
        }
    }

    private void updateUi(States state) {
        switch (state) {
            case Playing:
                binding.play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_pause, getTheme()));
                break;
            case Paused:
                binding.play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_play_aya, getTheme()));
                break;
        }
    }

    private void setCurrentPage(int num) {
        currentPage = num;
        rcMgr.setCurrentPage(num);
    }

    private void addHeader(int suraNum, String name) {
        TextView nameScreen = surahName(name);
        mainLinear.addView(nameScreen);
        if (suraNum != 1 && suraNum != 9)    // surat al-fatiha and At-Taubah
            mainLinear.addView(basmalah());
        if (action.equals("by_surah") && suraNum == surahIndex+1)
            target = nameScreen;
    }

    private int findMainSurah(ArrayList<ArrayList<Ayah>> surahs) {
        int largest = 0;
        for (int i = 1; i < surahs.size(); i++) {
            if (surahs.get(i).size() > surahs.get(largest).size())
                largest = i;
        }
        return largest;
    }

    private TextView surahName(String name) {
        TextView nameTv = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160);
        screenParams.bottomMargin = 20;
        nameTv.setLayoutParams(screenParams);
        nameTv.setPadding(0, 10, 0, 10);
        nameTv.setGravity(Gravity.CENTER);
        nameTv.setBackgroundColor(Color.TRANSPARENT);
        nameTv.setTextSize(textSize+5);
        if (theme.equals("ThemeM"))
            nameTv.setBackgroundResource(R.drawable.surah_header);
        else
            nameTv.setBackgroundResource(R.drawable.surah_header_light);

        nameTv.setTypeface(Typeface.DEFAULT_BOLD);

        nameTv.setText(name);
        return nameTv;
    }

    private TextView basmalah() {
        TextView nameScreen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameScreen.setLayoutParams(screenParams);
        nameScreen.setPadding(0, 0, 0, 10);
        nameScreen.setGravity(Gravity.CENTER);
        nameScreen.setTextSize(textSize);
        nameScreen.setTypeface(Typeface.DEFAULT_BOLD);
        nameScreen.setText("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ");
        if (theme.equals("ThemeM")) {
            nameScreen.setTextColor(Color.WHITE);
            nameScreen.setLinkTextColor(Color.WHITE);
        }
        else {
            nameScreen.setTextColor(Color.BLACK);
            nameScreen.setLinkTextColor(Color.BLACK);
        }
        return nameScreen;
    }

    private TextView screen() {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(screenParams);
        tv.setPadding(10, 0, 10, 30);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(textSize);
        if (theme.equals("ThemeM")) {
            tv.setTextColor(Color.WHITE);
            tv.setLinkTextColor(Color.WHITE);
        }
        else {
            tv.setTextColor(Color.BLACK);
            tv.setLinkTextColor(Color.BLACK);
        }
        Typeface typeface = Typeface.createFromAsset(getAssets(), "hafs_smart_08.ttf");
        tv.setTypeface(typeface);
        return tv;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        rcMgr.end();
        rcMgr = null;
    }
}
