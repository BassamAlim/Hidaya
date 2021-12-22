package bassamalim.hidaya.activities;

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

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.QuranActivityBinding;
import bassamalim.hidaya.helpers.Utils;
import bassamalim.hidaya.models.Ayah;
import bassamalim.hidaya.other.Constants;
import bassamalim.hidaya.popups.RecitationPopup;
import bassamalim.hidaya.popups.TafseerDialog;
import bassamalim.hidaya.replacements.DoubleClickLMM;
import bassamalim.hidaya.replacements.DoubleClickableSpan;
import bassamalim.hidaya.replacements.SwipeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private boolean scrolled;
    private MediaPlayer player1;
    private MediaPlayer player2;
    private WifiManager.WifiLock wifiLock;
    private Ayah selected;
    private Ayah lastPlayed;
    private Ayah lastTracked;
    private Object what;
    private boolean surahEnding;
    private int chosenSurah;

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

        player1 = new MediaPlayer();
        player2 = new MediaPlayer();

        what = new BackgroundColorSpan(getResources().getColor(R.color.track));
    }

    private void action(Intent intent) {
        switch (action) {
            case "by_surah":
                surahIndex = intent.getIntExtra("surah_index", 0);
                currentPage = getPage(surahIndex);
                break;
            case "by_page":
                currentPage = intent.getIntExtra("page", 0);
                break;
            case "random":
                currentPage = new Random().nextInt(QURAN_PAGES - 1);
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
            recitationsArr = new JSONArray(recitationString);
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
            if (player1.isPlaying() || player2.isPlaying()) {
                Log.i(Constants.TAG, "one is playing");
                if (lastPlayed != null)
                    selected = lastPlayed;

                stopPlaying();
            }
            else {
                Log.i(Constants.TAG, "no one is playing");
                if (selected == null)
                    selected = allAyahs.get(0);
                chosenSurah = selected.getSurah();
                setPlayers(selected);
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

    private void setPlayers(Ayah startAyah) {
        player1.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player2.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //mediaPlayer.setLooping(true);

        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock");
        wifiLock.acquire();

        AudioAttributes attributes = new AudioAttributes.Builder().setContentType(AudioAttributes
                .CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build();
        player1.setAudioAttributes(attributes);
        player2.setAudioAttributes(attributes);

        setPlayersListeners();

        lastPlayed = startAyah;

        preparePlayer(player1, startAyah);
    }

    private void setPlayersListeners() {
        player1.setOnPreparedListener(mediaPlayer -> {
            if (player2.isPlaying())
                player2.setNextMediaPlayer(player1);
            else {
                player1.start();
                track(lastPlayed);
                if (allAyahs.size() > lastPlayed.getIndex()+1)
                    preparePlayer(player2, allAyahs.get(lastPlayed.getIndex()+1));
            }
        });
        player1.setOnCompletionListener(mediaPlayer -> {
            if (allAyahs.size() > lastPlayed.getIndex()+1) {
                Ayah newAyah = allAyahs.get(lastPlayed.getIndex()+1);
                track(newAyah);
                if (allAyahs.size() > newAyah.getIndex()+1)
                    preparePlayer(player1, allAyahs.get(newAyah.getIndex()+1));
                lastPlayed = newAyah;
            }
            else
                ended();
        });
        player1.setOnErrorListener((mediaPlayer, i, i1) -> {
            Log.e(Constants.TAG, "Problem in player1");
            return true;
        });

        player2.setOnPreparedListener(mediaPlayer -> {
            if (player1.isPlaying()) {
                player1.setNextMediaPlayer(player2);
            }
            else {
                player2.start();
                track(lastPlayed);
                if (allAyahs.size() > lastPlayed.getIndex()+1)
                    preparePlayer(player2, allAyahs.get(lastPlayed.getIndex()+1));
            }
        });
        player2.setOnCompletionListener(mediaPlayer -> {
            if (allAyahs.size() > lastPlayed.getIndex()+1) {
                Ayah newAyah = allAyahs.get(lastPlayed.getIndex()+1);
                track(newAyah);
                if (allAyahs.size() > newAyah.getIndex()+1)
                    preparePlayer(player2, allAyahs.get(newAyah.getIndex()+1));
                lastPlayed = newAyah;
            }
            else
                ended();
        });
        player2.setOnErrorListener((mediaPlayer, i, i1) -> {
            Log.e(Constants.TAG, "Problem in player2");
            return true;
        });
    }

    private void preparePlayer(MediaPlayer player, Ayah ayah) {
        if (pref.getBoolean("stop_on_surah", false) && ayah.getSurah() != chosenSurah) {
            if (surahEnding)
                stopPlaying();
            else
                surahEnding = true;
            return;
        }

        player.reset();
        try {
            player.setDataSource(getApplicationContext(), getUri(ayah));
        } catch (IOException e) {e.printStackTrace();}
        player.prepareAsync();
    }

    private Uri getUri(Ayah ayah) {
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
        source += format(ayah.getSurah()) + format(ayah.getAyah()) + ".mp3";
        Log.i(Constants.TAG, source);
        return Uri.parse(source);
    }

    private void stopPlaying() {
        player1.reset();
        player2.reset();

        lastTracked.getSS().removeSpan(what);
        lastTracked.getScreen().setText(lastTracked.getSS());
        binding.play.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_play_blue, getTheme()));
    }

    private void ended() {
        Log.i(Constants.TAG, "Ended");
        Log.i(Constants.TAG, "last: " + lastPlayed.getIndex());
        Log.i(Constants.TAG, "size: " + allAyahs.size());
        if (pref.getBoolean("stop_on_page", false))
            stopPlaying();
        else if (currentPage < QURAN_PAGES && lastPlayed.getIndex()+1 == allAyahs.size()) {
            next();
            setPlayers(allAyahs.get(0));
        }
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
        if (player1 != null) {
            player1.release();
            player1 = null;
        }
        if (player2 != null) {
            player2.release();
            player2 = null;
        }
        if (wifiLock != null)
            wifiLock.release();
    }
}
