package com.bassamalim.athkar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
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

import com.bassamalim.athkar.other.Constants;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.QuranActivityBinding;
import com.bassamalim.athkar.dialogs.RecitationPopup;
import com.bassamalim.athkar.dialogs.TafseerDialog;
import com.bassamalim.athkar.helpers.Utils;
import com.bassamalim.athkar.models.Ayah;
import com.bassamalim.athkar.replacements.DoubleClickLMM;
import com.bassamalim.athkar.replacements.DoubleClickableSpan;
import com.bassamalim.athkar.replacements.SwipeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class QuranActivity extends SwipeActivity {

    private final int QURAN_PAGES = 604;
    private QuranActivityBinding binding;
    private SharedPreferences pref;
    private String action;
    private LinearLayout mainLinear;
    private JSONArray jsonArray;
    private JSONArray tafseerArray;
    private JSONArray recitationsArr;
    private int surahIndex;
    private int currentPage;
    private String currentPageText;
    private String currentSurah;
    private int textSize;
    private ArrayList<Ayah> allAyahs;
    private ArrayList<Ayah> arr;
    private TextView target;
    private boolean scrolled = false;
    private MediaPlayer mediaPlayer;
    private WifiManager.WifiLock wifiLock;
    private Ayah selected;
    private Ayah lastPlayed;
    private Spannable selectedSpannable;
    private Ayah lastTracked;
    private Object selectionWhat;
    private Object what;


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

        action(intent);

        buildPage(currentPage);
    }

    private void initiate() {
        mainLinear = binding.mainLinear;

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        textSize = pref.getInt(getString(R.string.quran_text_size_key), 30);

        mediaPlayer = new MediaPlayer();

        what = new BackgroundColorSpan(getResources().getColor(R.color.track));
    }

    private void action(Intent intent) {
        switch (action) {
            case "specific":
                surahIndex = intent.getIntExtra("surah_index", 0);
                currentPage = getPage(surahIndex);
                break;
            case "random":
                currentPage = new Random().nextInt(QURAN_PAGES - 1);
                break;
            case "bookmark":
                currentPage = intent.getIntExtra("page", 0);
                break;
        }
    }

    private void setupJson() {
        String jsonString = Utils.getJsonFromAssets(this, "hafs_smart_v8.json");
        String tafseerString = Utils.getJsonFromAssets(this, "tafseer.json");
        String recitationString = Utils.getJsonFromAssets(this, "recitations.json");
        try {
            assert jsonString != null;
            jsonArray = new JSONArray(jsonString);

            assert tafseerString != null;
            JSONObject mainTafseerObject = new JSONObject(tafseerString);
            JSONObject data = mainTafseerObject.getJSONObject("data");
            tafseerArray = data.getJSONArray("surahs");

            assert recitationString != null;
            JSONObject mainObj = new JSONObject(recitationString);
            recitationsArr = mainObj.getJSONArray("by_verse");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("myself", "error in setup json");
        }
    }

    private void setListeners() {
        binding.bookmarkButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            String text = currentPageText + ", " + currentSurah;
            editor.putInt("bookmarked_page", currentPage);
            editor.putString("bookmarked_text", text);
            editor.apply();
        });

        binding.play.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                if (lastPlayed != null)
                    selected = lastPlayed;
                mediaPlayer.stop();
                binding.play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_play_blue, getTheme()));
            }
            else {
                if (selected == null)
                    selected = allAyahs.get(0);
                else
                    selectedSpannable.removeSpan(selectionWhat);
                setPlayer(selected);
                binding.play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_stop, getTheme()));
                selected = null;
            }
        });

        binding.recitationSettings.setOnClickListener(v -> new RecitationPopup(this, v));
    }

    @Override
    protected void previous() {
        if (currentPage > 1) {
            buildPage(--currentPage);
            binding.scrollView.scrollTo(0, 0);
        }
    }

    @Override
    protected void next() {
        if (currentPage < QURAN_PAGES) {
            buildPage(++currentPage);
            Objects.requireNonNull(getSupportActionBar()).setTitle("رقم الصفحة " + currentPage);
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
            Log.e("myself", "error in get page");
        }
        return 0;
    }

    private int getPageStart(int pageNumber) {
        int start = 0;
        try {
            int counter = 0;
            while (jsonArray.getJSONObject(counter).getInt("page") < pageNumber)
                counter++;
            start = counter;
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("myself", "error in get page start");
        }
        return start;
    }

    private void buildPage(int pageNumber) {
        mainLinear.removeAllViews();
        allAyahs = new ArrayList<>();
        arr = new ArrayList<>();
        ArrayList<ArrayList<Ayah>> pageAyahs = new ArrayList<>();

        int counter = getPageStart(pageNumber);
        try {
            do {
                JSONObject ayah = jsonArray.getJSONObject(counter);
                int surahNum = ayah.getInt("sura_no");
                int ayahNum = ayah.getInt("aya_no");
                String ayahText = ayah.getString("aya_text");

                JSONObject tafseerSurah = tafseerArray.getJSONObject(surahNum-1);
                JSONArray tafseerAyahs = tafseerSurah.getJSONArray("ayahs");
                JSONObject tafseerAyah = tafseerAyahs.getJSONObject(ayahNum-1);
                String tafseer = tafseerAyah.getString("text");

                Ayah ayahModel = new Ayah(ayah.getInt("jozz"), surahNum, ayahNum,
                        ayah.getString("sura_name_ar"), ayahText + " ", tafseer);

                if (ayahNum == 1) {
                    if (arr.size() > 0) {
                        pageAyahs.add(arr);
                        publish(arr);
                    }

                    addHeader(surahNum, ayah.getString("sura_name_ar"));
                }
                arr.add(ayahModel);

            } while (++counter != 6236 && jsonArray.getJSONObject(counter)
                    .getInt("page") == pageNumber);
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "trouble in building page");
        }

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
                public void onClick(@NonNull View widget) {}
                @Override
                public void onClick(Spannable buffer, Object what) {
                    selected = allAyahs.get(list.get(finalI).getIndex());
                    selectedSpannable = buffer;
                    selectionWhat = what;
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
        screen.setText(ss);
        screen.setMovementMethod(DoubleClickLMM.getInstance(getResources().getColor(
                R.color.light_secondary)));
        mainLinear.addView(screen);
        arr = new ArrayList<>();
    }

    private void finalize(int juz, String name) {
        String juzText = "جزء " + juz;
        currentSurah = "سُورَة " + name;
        currentPageText = "صفحة " + currentPage;
        binding.juzNumber.setText(juzText);
        binding.suraName.setText(currentSurah);
        binding.pageNumber.setText(currentPageText);

        ScrollView scroll = binding.scrollView;
        if (action.equals("specific") && !scrolled) {
            long delay = 100; //delay to let finish with possible modifications to ScrollView
            scroll.postDelayed(() -> scroll.smoothScrollTo(0, target.getTop()), delay);
            scrolled = true;
        }
    }

    private void track(Ayah subject) {
        if (lastTracked != null) {
            lastTracked.getSS().removeSpan(what);
            lastTracked.getScreen().setText(lastTracked.getSS());
        }
        subject.getSS().setSpan(what, subject.getStart(), subject.getEnd(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        subject.getScreen().setText(subject.getSS());    // heavy, but the only working way
        lastTracked = subject;
    }

    private void setPlayer(Ayah ayah) {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //mediaPlayer.setLooping(true);
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock");
        wifiLock.acquire();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        play(ayah);
    }

    private void play(Ayah ayah) {
        String text = getSource();
        text += format(ayah.getSurah());
        text += format(ayah.getAyah());
        text += ".mp3";

        Log.i(Constants.TAG, text);
        Uri uri = Uri.parse(text);
        ayah.getSS().setSpan(what, ayah.getStart(), ayah.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                track(ayah);
                lastPlayed = ayah;
                mediaPlayer.start();
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(mp -> {
            try {
                play(allAyahs.get(ayah.getIndex()+1));
            }
            catch (Exception e) {
                boolean playNext = pref.getBoolean("play_next_page", true);
                if (playNext) {
                    next();
                    play(allAyahs.get(0));
                }
                else {
                    binding.play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_play_blue, getTheme()));
                }
            }
        });
    }

    private String getSource() {
        String source = "https://www.everyayah.com/data/";
        int choice = pref.getInt("chosen_reciter", 13);
        try {
            JSONObject reciter = recitationsArr.getJSONObject(choice);
            if (reciter.getString("128").length() != 0)
                source += reciter.getString("128");
            else if (reciter.getString("64").length() != 0)
                source += reciter.getString("64");
            else if (reciter.getString("192").length() != 0)
                source += reciter.getString("192");
            else if (reciter.getString("40").length() != 0)
                source += reciter.getString("40");
            else source += reciter.getString("30");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "Problems in getSource");
        }
        return source;
    }

    private void addHeader(int num, String name) {
        TextView nameScreen = surahName(name);
        mainLinear.addView(nameScreen);
        if (num != 9) {    // surat At-Taubah has no basmalah
            TextView basmalah = basmalah();
            mainLinear.addView(basmalah);
        }
        if (action.equals("specific") && num == surahIndex+1)
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

    private String format(int in) {
        String strIn = String.valueOf(in);
        String out = "";
        if (strIn.length() == 1)
            out += "00";
        else if (strIn.length() == 2)
            out += "0";
        out += strIn;
        return out;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (wifiLock != null)
            wifiLock.release();
    }
}
