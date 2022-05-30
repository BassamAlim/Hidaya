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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bassamalim.hidaya.R;
import bassamalim.hidaya.adapters.RecyclerQuranViewerAdapter;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AyatDB;
import bassamalim.hidaya.databinding.ActivityQuranViewerBinding;
import bassamalim.hidaya.dialogs.InfoDialog;
import bassamalim.hidaya.dialogs.QuranSettingsDialog;
import bassamalim.hidaya.dialogs.TutorialDialog;
import bassamalim.hidaya.enums.States;
import bassamalim.hidaya.helpers.AyahPlayer;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.replacements.DoubleClickLMM;
import bassamalim.hidaya.replacements.DoubleClickableSpan;
import bassamalim.hidaya.replacements.SwipeActivity;

public class QuranViewer extends SwipeActivity {

    private ActivityQuranViewerBinding binding;
    private AppDatabase db;
    private SharedPreferences pref;
    private String action;
    private ViewFlipper flipper;
    private ScrollView[] scrollViews;
    private LinearLayout[] lls;
    private RecyclerView[] recyclers;
    private RecyclerQuranViewerAdapter adapter;
    private int currentView;
    private int surahIndex;
    private int currentPage;
    private String currentPageText;
    private String currentSurah;
    private int textSize;
    private final List<Ayah> allAyahs = new ArrayList<>();
    private List<String> names;
    private List<Ayah> arr;
    private TextView target;
    private boolean scrolled;
    private Ayah selected;
    private AyahPlayer ayahPlayer;
    private List<AyatDB> ayatDB;
    private String theme;
    private String language;
    private String viewType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeify();
        binding = ActivityQuranViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initiate();

        checkFirstTime();

        action = getIntent().getAction();
        action(getIntent());

        if (viewType.equals("list"))
            setupRecyclers();

        buildPage(currentPage);

        setListeners();

        setupPlayer();
    }

    private void themeify() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        language = Utils.onActivityCreateSetLocale(this);
        textSize = pref.getInt(getString(R.string.quran_text_size_key), 30);
        theme = pref.getString(getString(R.string.theme_key), getString(R.string.default_theme));

        viewType = language.equals("en") ? "list" :
                pref.getString("quran_view_type", "page");

        switch (theme) {
            case "ThemeL":
                setTheme(R.style.QuranL);
                break;
            case "ThemeM":
                setTheme(R.style.QuranM);
                break;
        }
    }

    private void initiate() {
        flipper = binding.flipper;
        scrollViews = new ScrollView[]{binding.scrollview1, binding.scrollview2};
        lls = new LinearLayout[]{binding.linear1, binding.linear2};

        db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        ayatDB = db.ayahDao().getAll();
        if (language.equals("en"))
            names = db.suarDao().getNamesEn();
        else
            names = db.suarDao().getNames();
    }

    private void checkFirstTime() {
        if (pref.getBoolean("is_first_time_in_quran", true))
            new TutorialDialog(this, getString(R.string.quran_tips),
                    "is_first_time_in_quran").show(getSupportFragmentManager(),
                    TutorialDialog.TAG);
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
                setCurrentPage(new Random().nextInt(Global.QURAN_PAGES - 1));
                break;
        }
    }

    private void setupRecyclers() {
        recyclers = new RecyclerView[]{binding.recycler1, binding.recycler2};

        LinearLayoutManager[] layoutManagers = new LinearLayoutManager[]
                {new LinearLayoutManager(this), new LinearLayoutManager(this)};
        recyclers[0].setLayoutManager(layoutManagers[0]);
        recyclers[1].setLayoutManager(layoutManagers[1]);

        if (action.equals("by_surah"))
            adapter = new RecyclerQuranViewerAdapter(
                    this, allAyahs, theme, language,  surahIndex);
        else
            adapter = new RecyclerQuranViewerAdapter(
                    this, allAyahs, theme, language, -1);

        recyclers[0].setAdapter(adapter);
        recyclers[1].setAdapter(adapter);

        flipper.setDisplayedChild(2);
    }

    private int getPage(int surahIndex) {
        return db.suarDao().getPage(surahIndex);
    }

    private void setCurrentPage(int num) {
        currentPage = num;
        if (ayahPlayer != null)
            ayahPlayer.setCurrentPage(num);
    }

    private void buildPage(int pageNumber) {
        if (viewType.equals("page"))
            lls[currentView].removeAllViews();
        allAyahs.clear();
        arr = new ArrayList<>();
        List<List<Ayah>> pageAyahs = new ArrayList<>();

        int counter = getPageStart(pageNumber);
        do {
            AyatDB aya = ayatDB.get(counter);
            int suraNum = aya.getSura_no();    // starts from 1
            int ayaNum = aya.getAya_no();

            Ayah ayahModel = new Ayah(aya.getJozz(), suraNum, ayaNum, names.get(suraNum-1),
                    aya.getAya_text() + " ", aya.getAya_translation_en(),
                    aya.getAya_tafseer());

            if (ayaNum == 1) {
                if (arr.size() > 0) {
                    pageAyahs.add(arr);
                    if (viewType.equals("page"))
                        publishPage(arr);
                }
                if (viewType.equals("page"))
                    addHeader(suraNum, ayahModel.getSurahName());
            }

            arr.add(ayahModel);
        } while (++counter != Global.QURAN_AYAS && ayatDB.get(counter).getPage() == pageNumber);

        int juz = arr.get(0).getJuz();

        pageAyahs.add(arr);

        if (viewType.equals("list")) {
            publishList(arr);
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
        else
            publishPage(arr);

        finalize(juz, pageAyahs.get(findMainSurah(pageAyahs)).get(0).getSurahName());
    }

    private void publishPage(List<Ayah> list) {
        TextView screen = screen();
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
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
                    new InfoDialog(getString(R.string.tafseer), list.get(finalI).getTafseer())
                            .show(getSupportFragmentManager(), InfoDialog.TAG);
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

        if (ayahPlayer != null)
            ayahPlayer.setAllAyahsSize(allAyahs.size());

        screen.setText(ss);

        getContainer().addView(screen);

        arr = new ArrayList<>();
    }

    private void publishList(List<Ayah> list) {
        for (int i = 0; i < list.size(); i++) {
            int finalI = i;

            SpannableString ss = new SpannableString(list.get(i).getText());
            DoubleClickableSpan clickableSpan = new DoubleClickableSpan() {
                @Override
                public void onDoubleClick(View textView) {
                    new InfoDialog(getString(R.string.tafseer), list.get(finalI).getTafseer())
                            .show(getSupportFragmentManager(), InfoDialog.TAG);
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
            ss.setSpan(clickableSpan, 0, list.get(i).getText().length()-1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            list.get(finalI).setIndex(allAyahs.size());
            allAyahs.add(list.get(finalI));
        }

        if (ayahPlayer != null)
            ayahPlayer.setAllAyahsSize(allAyahs.size());

        arr = new ArrayList<>();
    }

    private void finalize(int juz, String name) {
        String juzText = getString(R.string.juz) + " " +
                Utils.translateNumbers(this, String.valueOf(juz));
        currentSurah = getString(R.string.sura) + " " + name;
        currentPageText = getString(R.string.page) + " " +
                Utils.translateNumbers(this, String.valueOf(currentPage));
        binding.juzNumber.setText(juzText);
        binding.suraName.setText(currentSurah);
        binding.pageNumber.setText(currentPageText);

        if (action.equals("by_surah") && !scrolled) {
            long delay = 100;    //delay to let finish with possible modifications to ScrollView
            if (viewType.equals("list"))
                recyclers[currentView].smoothScrollToPosition(0);
            else
                scrollViews[currentView].postDelayed(() ->
                        scrollViews[currentView].smoothScrollTo(0, target.getTop()), delay);
            scrolled = true;
        }
    }

    @Override
    protected void previous() {
        if (currentPage > 1) {
            flipper.setInAnimation(this, R.anim.slide_in_right);
            flipper.setOutAnimation(this, R.anim.slide_out_left);
            currentView = (currentView + 1) % 2;
            setCurrentPage(--currentPage);
            buildPage(currentPage);
            flip();
        }
    }

    @Override
    protected void next() {
        if (currentPage < Global.QURAN_PAGES) {
            flipper.setInAnimation(this, R.anim.slide_in_left);
            flipper.setOutAnimation(this, R.anim.slide_out_right);
            currentView = (currentView + 1) % 2;
            setCurrentPage(++currentPage);
            buildPage(currentPage);
            flip();
        }
    }

    private void flip() {
        if (viewType.equals("list")) {
            if (flipper.getDisplayedChild() == 2)
                flipper.setDisplayedChild(3);
            else
                flipper.setDisplayedChild(2);

            recyclers[currentView].scrollTo(0, 0);
        }
        else {
            if (flipper.getDisplayedChild() == 0)
                flipper.setDisplayedChild(1);
            else
                flipper.setDisplayedChild(0);

            scrollViews[currentView].scrollTo(0, 0);
        }
    }

    private ViewGroup getContainer() {
        if (viewType.equals("list"))
            return recyclers[currentView];
        return lls[currentView];
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

    private void setListeners() {
        binding.bookmarkButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("bookmarked_page", currentPage);
            editor.putString("bookmarked_text", currentPageText + ", " + currentSurah);
            editor.apply();

            Toast.makeText(this, getString(R.string.page_bookmarked),
                    Toast.LENGTH_SHORT).show();
        });

        binding.play.setOnClickListener(view -> {
            if (ayahPlayer.getState() == States.Playing) {
                if (ayahPlayer.getLastPlayed() != null)
                    selected = ayahPlayer.getLastPlayed();

                ayahPlayer.pause();
                updateUi(States.Paused);
            }
            else if (ayahPlayer.getState() == States.Paused) {
                if (selected == null)
                    ayahPlayer.resume();
                else {
                    ayahPlayer.setChosenSurah(selected.getSurahNum());
                    ayahPlayer.requestPlay(selected);
                }
                updateUi(States.Playing);
            }
            else {
                if (selected == null)
                    selected = allAyahs.get(0);

                ayahPlayer.setChosenSurah(selected.getSurahNum());
                ayahPlayer.requestPlay(selected);

                updateUi(States.Playing);
            }
            selected = null;
        });
        binding.prevAyah.setOnClickListener(view -> ayahPlayer.prevAyah());
        binding.nextAyah.setOnClickListener(view -> ayahPlayer.nextAyah());

        binding.recitationSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuranSettingsDialog.class);
            settingsDialog.launch(intent);
        });
    }

    private int getPageStart(int pageNumber) {
        int start;
        int counter = 0;
        while (ayatDB.get(counter).getPage() < pageNumber)
            counter++;
        start = counter;

        return start;
    }

    private final ActivityResultLauncher<Intent> settingsDialog = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            viewType = data.getStringExtra("view_type");
            textSize = data.getIntExtra("text_size", 30);

            flipper.setInAnimation(null);
            flipper.setOutAnimation(null);
            if (viewType.equals("list")) {
                setupRecyclers();

                adapter.setTextSize(textSize);
                recyclers[0].setAdapter(null);
                recyclers[1].setAdapter(null);
                recyclers[0].setAdapter(adapter);
                recyclers[1].setAdapter(adapter);
            }
            else
                flipper.setDisplayedChild(0);

            buildPage(currentPage);
            if (ayahPlayer != null)
                ayahPlayer.setViewType(viewType);
        }
    });

    private void setupPlayer() {
        ayahPlayer = new AyahPlayer(this);
        ayahPlayer.setCurrentPage(currentPage);
        ayahPlayer.setViewType(viewType);

        AyahPlayer.Coordinator uiListener = new AyahPlayer.Coordinator() {
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
        ayahPlayer.setCoordinator(uiListener);

        ayahPlayer.setAllAyahsSize(allAyahs.size());
    }

    private int findMainSurah(List<List<Ayah>> surahs) {
        int largest = 0;
        for (int i = 1; i < surahs.size(); i++) {
            if (surahs.get(i).size() > surahs.get(largest).size())
                largest = i;
        }
        return largest;
    }

    private void addHeader(int suraNum, String name) {
        TextView nameScreen = surahName(name);

        getContainer().addView(nameScreen);
        if (suraNum != 1 && suraNum != 9)    // surat al-fatiha and At-Taubah
            getContainer().addView(basmalah());

        if (action.equals("by_surah") && suraNum == surahIndex+1)
            target = nameScreen;
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
        if (theme.equals("ThemeL"))
            nameTv.setBackgroundResource(R.drawable.surah_header_light);
        else
            nameTv.setBackgroundResource(R.drawable.surah_header);

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
        if (theme.equals("ThemeL")) {
            nameScreen.setTextColor(Color.BLACK);
            nameScreen.setLinkTextColor(Color.BLACK);
        }
        else {
            nameScreen.setTextColor(Color.WHITE);
            nameScreen.setLinkTextColor(Color.WHITE);
        }
        return nameScreen;
    }

    private TextView screen() {
        TextView tv = (TextView) getLayoutInflater().inflate(R.layout.tv_quran_viewer, null);
        tv.setTextSize(textSize);
        if (theme.equals("ThemeL"))
            tv.setMovementMethod(DoubleClickLMM.getInstance(
                    getResources().getColor(R.color.highlight_L, getTheme())));
        else
            tv.setMovementMethod(DoubleClickLMM.getInstance(
                    getResources().getColor(R.color.highlight_M, getTheme())));
        return tv;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        ayahPlayer.finish();
        ayahPlayer = null;
    }
}
