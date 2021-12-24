package bassamalim.hidaya.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.QuranActivityBinding;
import bassamalim.hidaya.enums.States;
import bassamalim.hidaya.helpers.RecitationManager;
import bassamalim.hidaya.helpers.Utils;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.models.JAyah;
import bassamalim.hidaya.models.JTafseer;
import bassamalim.hidaya.other.Constants;
import bassamalim.hidaya.popups.RecitationPopup;
import bassamalim.hidaya.popups.TafseerDialog;
import bassamalim.hidaya.replacements.DoubleClickLMM;
import bassamalim.hidaya.replacements.DoubleClickableSpan;
import bassamalim.hidaya.replacements.SwipeActivity;

public class QuranActivity extends SwipeActivity {

    private final int QURAN_PAGES = 604;
    private QuranActivityBinding binding;
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
    private JAyah[] jAyah;
    private JTafseer jTafseer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = QuranActivityBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.infoBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        initiate();

        setListeners();

        Intent intent = getIntent();
        action = intent.getAction();

        setupJson();

        setupManager();

        action(intent);

        buildPage(currentPage);
    }

    private void initiate() {
        mainLinear = binding.mainLinear;

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        textSize = pref.getInt(getString(R.string.quran_text_size_key), 30);
    }

    private void action(Intent intent) {
        switch (action) {
            case "by_surah":
                surahIndex = intent.getIntExtra("surah_index", 0);
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

    private void setupJson() {
        long t1 = System.currentTimeMillis();
        long temp = System.currentTimeMillis();
        long now;

        String jsonString = Utils.getJsonFromAssets(this, "hafs_smart_v8.json");
        String tafseerString = Utils.getJsonFromAssets(this, "tafseer.json");
        Gson gson = new Gson();

        now = System.currentTimeMillis();
        Log.i(Constants.TAG, "t1: " + (now-temp));
        temp = now;

        jAyah = gson.fromJson(jsonString, JAyah[].class);
        jTafseer = gson.fromJson(tafseerString, JTafseer.class);

        now = System.currentTimeMillis();
        Log.i(Constants.TAG, "t2: " + (now-temp));
        Log.i(Constants.TAG, "Full Time: " + (System.currentTimeMillis()-t1));
    }

    private void setListeners() {
        binding.bookmarkButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            String text = currentPageText + ", " + currentSurah;
            editor.putInt("bookmarked_page", currentPage);
            editor.putString("bookmarked_text", text);
            editor.apply();
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
                rcMgr.resume();
                updateUi(States.Playing);
            }
            else {
                if (selected == null)
                    selected = allAyahs.get(0);

                rcMgr.setChosenSurah(selected.getSurah());
                rcMgr.setPlayers(selected);

                updateUi(States.Playing);
                selected = null;
            }
        });
        binding.nextAyah.setOnClickListener(view -> rcMgr.nextAyah());
        binding.recitationSettings.setOnClickListener(v -> new RecitationPopup(this, v));
    }

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
        try {
            String pagesString = Utils.getJsonFromAssets(this, "pages.json");
            assert pagesString != null;
            JSONArray pages = new JSONArray(pagesString);
            JSONObject page = pages.getJSONObject(surahIndex);
            return page.getInt("page");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "error in get page");
        }
        return 0;
    }

    private int getPageStart(int pageNumber) {
        int start;
        int counter = 0;
        while (jAyah[counter].getPage() < pageNumber)
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
            JAyah ayah = jAyah[counter];
            int surahNum = ayah.getSura_no();
            int ayahNum = ayah.getAya_no();
            String ayahText = ayah.getAya_text();

            JTafseer.Data.Surah tafseerSurah = jTafseer.getData().getSurahs()[surahNum-1];
            JTafseer.Data.Surah.JTAyah[] tafseerAyahs = tafseerSurah.getAyahs();
            JTafseer.Data.Surah.JTAyah tafseerAyah = tafseerAyahs[ayahNum-1];

            String tafseer = tafseerAyah.getText();

            Ayah ayahModel = new Ayah(ayah.getJozz(), surahNum, ayahNum,
                    ayah.getSura_name_ar(), ayahText + " ", tafseer);

            if (ayahNum == 1) {
                if (arr.size() > 0) {
                    pageAyahs.add(arr);
                    publish(arr);
                }

                addHeader(surahNum, ayahModel.getSurahName());
            }
            arr.add(ayahModel);

        } while (++counter != 6236 && jAyah[counter].getPage() == pageNumber);

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
        for (int i=0; i<list.size(); i++) {
            int finalI = i;
            DoubleClickableSpan clickableSpan = new DoubleClickableSpan() {
                @Override
                public void onDoubleClick(View textView) {
                    new TafseerDialog(list.get(finalI).getTafseer()).show(
                            getSupportFragmentManager(), TafseerDialog.TAG);
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
        screen.setMovementMethod(DoubleClickLMM.getInstance(getResources().getColor(
                R.color.light_secondary)));
        mainLinear.addView(screen);
        arr = new ArrayList<>();
    }

    private void finalize(int juz, String name) {
        String juzText = "جزء " + translateNumbers(String.valueOf(juz));
        currentSurah = "سُورَة " + name;
        currentPageText = "صفحة " + translateNumbers(String.valueOf(currentPage));
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
                        R.drawable.ic_stop, getTheme()));
                break;
            case Paused:
                binding.play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_play_blue, getTheme()));
                break;
        }
    }

    private void setCurrentPage(int num) {
        currentPage = num;
        rcMgr.setCurrentPage(num);
    }

    private void addHeader(int num, String name) {
        TextView nameScreen = surahName(name);
        mainLinear.addView(nameScreen);
        if (num != 9) {    // surat At-Taubah has no basmalah
            TextView basmalah = basmalah();
            mainLinear.addView(basmalah);
        }
        if (action.equals("by_surah") && num == surahIndex+1)
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
        TextView nameScreen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.bottomMargin = 20;
        nameScreen.setLayoutParams(screenParams);
        nameScreen.setPadding(0, 10, 0, 10);
        nameScreen.setGravity(Gravity.CENTER);
        nameScreen.setBackground(AppCompatResources
                .getDrawable(this, R.drawable.surah_header));
        nameScreen.setTextSize(textSize+5);
        nameScreen.setTypeface(Typeface.DEFAULT_BOLD);
        nameScreen.setText(name);
        return nameScreen;
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
        nameScreen.setTextColor(Color.WHITE);
        nameScreen.setText("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ");
        return nameScreen;
    }

    private TextView screen() {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screen.setLayoutParams(screenParams);
        screen.setPadding(0, 0, 0, 30);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "hafs_smart_08.ttf");
        screen.setTypeface(typeface);
        screen.setTextColor(Color.WHITE);
        screen.setLinkTextColor(Color.WHITE);
        return screen;
    }

    private String translateNumbers(String english) {
        String result;
        HashMap<Character, Character> map = new HashMap<>();
        map.put('0', '٠');
        map.put('1', '١');
        map.put('2', '٢');
        map.put('3', '٣');
        map.put('4', '٤');
        map.put('5', '٥');
        map.put('6', '٦');
        map.put('7', '٧');
        map.put('8', '٨');
        map.put('9', '٩');
        map.put('A', 'ص');
        map.put('P', 'م');

        if (english.charAt(0) == '0')
            english = english.replaceFirst("0", "");

        StringBuilder temp = new StringBuilder();
        for (int j = 0; j < english.length(); j++) {
            char t = english.charAt(j);
            if (map.containsKey(t))
                t = map.get(t);
            temp.append(t);
        }
        result = temp.toString();

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        rcMgr.end();
        rcMgr = null;
    }
}
