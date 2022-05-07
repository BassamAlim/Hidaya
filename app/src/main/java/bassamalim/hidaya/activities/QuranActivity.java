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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
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
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.AyatDB;
import bassamalim.hidaya.databinding.ActivityQuranBinding;
import bassamalim.hidaya.dialogs.InfoDialog;
import bassamalim.hidaya.dialogs.QuranSettingsDialog;
import bassamalim.hidaya.dialogs.TutorialDialog;
import bassamalim.hidaya.enums.States;
import bassamalim.hidaya.helpers.AyahPlayer;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.replacements.DoubleClickLMM;
import bassamalim.hidaya.replacements.DoubleClickableSpan;
import bassamalim.hidaya.replacements.SwipeActivity;

public class QuranActivity extends SwipeActivity {

    private final int QURAN_PAGES = 604;
    private ActivityQuranBinding binding;
    private AppDatabase db;
    private SharedPreferences pref;
    private String action;
    private ViewFlipper flipper;
    private LinearLayout[] lls;
    private int currentLinear;
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
    private AyahPlayer ayahPlayer;
    private List<AyatDB> ayatDB;
    private String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeify();
        Utils.onActivityCreateSetLocale(this);
        binding = ActivityQuranBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initiate();

        checkFirstTime();

        setListeners();

        Intent intent = getIntent();
        action = intent.getAction();
        action(intent);

        buildPage(currentPage);

        setupPlayer();
    }

    private void themeify() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        textSize = pref.getInt(getString(R.string.quran_text_size_key), 30);
        theme = pref.getString(getString(R.string.theme_key), getString(R.string.default_theme));

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
        flipper.setMeasureAllChildren(false);
        lls = new LinearLayout[]{binding.linear1, binding.linear2};

        db = Room.databaseBuilder(this, AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();
        ayatDB = db.ayahDao().getAll();
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
                setCurrentPage(new Random().nextInt(QURAN_PAGES - 1));
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
        binding.prevAyah.setOnClickListener(view -> ayahPlayer.prevAyah());
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
                    ayahPlayer.setChosenSurah(selected.getSurah());
                    ayahPlayer.requestPlay(selected);
                }
                updateUi(States.Playing);
            }
            else {
                if (selected == null)
                    selected = allAyahs.get(0);

                ayahPlayer.setChosenSurah(selected.getSurah());
                ayahPlayer.requestPlay(selected);

                updateUi(States.Playing);
            }
            selected = null;
        });
        binding.nextAyah.setOnClickListener(view -> ayahPlayer.nextAyah());
        binding.recitationSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuranSettingsDialog.class);
            settingsDialog.launch(intent);
        });
    }

    ActivityResultLauncher<Intent> settingsDialog = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    textSize = data.getIntExtra("text_size", 30);
                    buildPage(currentPage);
                }
            });

    private void setupPlayer() {
        ayahPlayer = new AyahPlayer(this);
        ayahPlayer.setCurrentPage(currentPage);

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
    }

    @Override
    protected void previous() {
        if (currentPage > 1) {
            flipper.setInAnimation(this, R.anim.slide_in_right);
            flipper.setOutAnimation(this, R.anim.slide_out_left);
            currentLinear = (currentLinear + 1) % 2;
            setCurrentPage(--currentPage);
            buildPage(currentPage);
            flipper.showPrevious();
            binding.scrollView.scrollTo(0, 0);
        }
    }

    @Override
    protected void next() {
        if (currentPage < QURAN_PAGES) {
            flipper.setInAnimation(this, R.anim.slide_in_left);
            flipper.setOutAnimation(this, R.anim.slide_out_right);
            currentLinear = (currentLinear + 1) % 2;
            setCurrentPage(++currentPage);
            buildPage(currentPage);
            flipper.showNext();
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
        lls[currentLinear].removeAllViews();
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
                    new InfoDialog(getString(R.string.tafseer), list.get(finalI).getTafseer()).show(
                            getSupportFragmentManager(), InfoDialog.TAG);
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
        if (theme.equals("ThemeL"))
            screen.setMovementMethod(DoubleClickLMM.getInstance(
                getResources().getColor(R.color.highlight_L, getTheme())));
        else
            screen.setMovementMethod(DoubleClickLMM.getInstance(
                    getResources().getColor(R.color.highlight_M, getTheme())));


        lls[currentLinear].addView(screen);

        arr = new ArrayList<>();
    }

    private void finalize(int juz, String name) {
        String juzText = getString(R.string.juz) + " " + Utils.translateNumbers(
                this, String.valueOf(juz));
        currentSurah = getString(R.string.sura) + " " + name;
        currentPageText = getString(R.string.page) + " " + Utils.translateNumbers(
                this, String.valueOf(currentPage));
        binding.juzNumber.setText(juzText);
        binding.suraName.setText(currentSurah);
        binding.pageNumber.setText(currentPageText);

        if (action.equals("by_surah") && !scrolled) {
            long delay = 100; //delay to let finish with possible modifications to ScrollView
            ScrollView scroll = binding.scrollView;
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
        if (ayahPlayer != null)
            ayahPlayer.setCurrentPage(num);
    }

    private void addHeader(int suraNum, String name) {
        TextView nameScreen = surahName(name);

        lls[currentLinear].addView(nameScreen);
        if (suraNum != 1 && suraNum != 9)    // surat al-fatiha and At-Taubah
            lls[currentLinear].addView(basmalah());

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
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(screenParams);
        tv.setPadding(5, 0, 5, 30);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(textSize);
        TypedValue specialTextColor = new TypedValue();
        getTheme().resolveAttribute(R.attr.mySpecialText, specialTextColor, true);
        tv.setTextColor(specialTextColor.data);
        tv.setLinkTextColor(specialTextColor.data);
        tv.setTypeface(ResourcesCompat.getFont(this, R.font.hafs_smart_08));
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
