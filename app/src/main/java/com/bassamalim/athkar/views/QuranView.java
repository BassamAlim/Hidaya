package com.bassamalim.athkar.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.DoubleClickLMM;
import com.bassamalim.athkar.DoubleClickableSpan;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.SwipeActivity;
import com.bassamalim.athkar.Utils;
import com.bassamalim.athkar.databinding.QuranViewBinding;
import com.bassamalim.athkar.dialogs.TafseerDialog;
import com.bassamalim.athkar.models.Ayah;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class QuranView extends SwipeActivity {

    private QuranViewBinding binding;
    private LinearLayout mainLinear;
    private JSONArray jsonArray;
    private JSONArray tafseerArray;
    private int currentPage;
    private int textSize;
    private ArrayList<Ayah> arr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = QuranViewBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.infoBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        mainLinear = binding.mainLinear;

        Intent intent = getIntent();
        String action = intent.getAction();

        setupJson();

        textSize = getSize();

        if (action.equals("specific")) {
            int surahIndex = intent.getIntExtra("surah_index", 0);
            currentPage = getPage(surahIndex);
        }
        else if (action.equals("random"))
            currentPage = new Random().nextInt(Constants.QURAN_PAGES-1);

        buildPage(currentPage);
    }

    @Override
    protected void previous() {
        if (currentPage < Constants.QURAN_PAGES) {
            buildPage(++currentPage);
            binding.scrollView.scrollTo(0, 0);
        }
    }

    @Override
    protected void next() {
        if (currentPage > 1) {
            buildPage(--currentPage);
            Objects.requireNonNull(getSupportActionBar()).setTitle("رقم الصفحة " + currentPage);
            binding.scrollView.scrollTo(0, 0);
        }
    }

    private void setupJson() {
        String jsonString = Utils.getJsonFromAssets(this, "hafs_smart_v8.json");
        String tafseerString = Utils.getJsonFromAssets(this, "tafseer.json");
        try {
            assert jsonString != null;
            jsonArray = new JSONArray(jsonString);

            assert tafseerString != null;
            JSONObject mainTafseerObject = new JSONObject(tafseerString);
            JSONObject data = mainTafseerObject.getJSONObject("data");
            tafseerArray = data.getJSONArray("surahs");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("myself", "error in setup json");
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

    private void infoBar(int juz, String name) {
        String juzText = "جزء " + juz;
        String surahNameText = "سُورَة " + name;
        String pageNumberText = "صفحة " + currentPage;
        binding.juzNumber.setText(juzText);
        binding.suraName.setText(surahNameText);
        binding.pageNumber.setText(pageNumberText);
    }

    private void buildPage(int pageNumber) {
        mainLinear.removeAllViews();
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

                Ayah ayahModel = new Ayah(ayah.getInt("jozz"),
                        ayah.getString("sura_name_ar"), ayahText + " ", tafseer);

                if (ayahNum == 1) {
                    if (arr.size() > 0) {
                        pageAyahs.add(arr);
                        publish(arr);
                    }

                    TextView nameScreen = surahName(ayah.getString("sura_name_ar"));
                    TextView basmalah = basmalah();
                    mainLinear.addView(nameScreen);
                    mainLinear.addView(basmalah);
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

        infoBar(juz, pageAyahs.get(findMainSurah(pageAyahs)).get(0).getSurahName());
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
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            ss.setSpan(clickableSpan, list.get(i).getStart(), list.get(i).getEnd(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        screen.setText(ss);
        screen.setMovementMethod(DoubleClickLMM.getInstance());
        mainLinear.addView(screen);
        arr = new ArrayList<>();
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
        nameScreen.setBackground(AppCompatResources.
                getDrawable(this, R.drawable.surah_header));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            nameScreen.setTextColor(getResources().getColor(R.color.accent, getTheme()));
        else
            nameScreen.setTextColor(getResources().getColor(R.color.accent));
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
        screen.setLinkTextColor(Color.WHITE);

        return screen;
    }

    private int getSize() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(getString(R.string.quran_text_size_key), 30);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
