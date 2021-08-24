package com.bassamalim.athkar.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.Utils;
import com.bassamalim.athkar.databinding.QuranViewBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class QuranView extends AppCompatActivity {

    private QuranViewBinding binding;
    private LinearLayout mainLinear;
    private JSONObject jsonObject;
    private JSONArray surahs;
    private int currentPage;
    private int textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = QuranViewBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        setupJson();

        int surahIndex = intent.getIntExtra("surah index", 0);

        currentPage = getPage(surahIndex);

        mainLinear = binding.mainLinear;

        Objects.requireNonNull(getSupportActionBar()).setTitle("رقم الصفحة " + currentPage);

        textSize = getSize();

        buildPage(currentPage);

        setListeners();
    }

    private void setupJson() {
        String jsonString = Utils.getJsonFromAssets(this, "quran.json");
        try {
            assert jsonString != null;
            jsonObject = new JSONObject(jsonString);
            JSONObject data = jsonObject.getJSONObject("data");
            surahs = data.getJSONArray("surahs");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.i("myself", "error in setup json");
        }
    }

    private int getPage(int surahIndex) {
        try {
            JSONObject surah = surahs.getJSONObject(surahIndex);
            JSONArray ayahs = surah.getJSONArray("ayahs");
            JSONObject ayah = ayahs.getJSONObject(0);
            return ayah.getInt("page");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.i("myself", "error in get page");
        }
        return 0;
    }

    public int[] getPageStart(int pageNumber) {
        int[] start = new int[2];
        try {
            String startString = Utils.getJsonFromAssets(this, "start.json");
            assert startString != null;
            jsonObject = new JSONObject(startString);
            JSONArray pages = jsonObject.getJSONArray("pages");
            JSONObject page = pages.getJSONObject(pageNumber-1);
            start[0] = page.getInt("surah");
            start[1] = page.getInt("ayah");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.i("myself", "error in get page start");
        }
        return start;
    }

    public void buildPage(int pageNumber) {
        mainLinear.removeAllViews();
        JSONObject surah;
        JSONArray ayahs;
        JSONObject ayah;

        int[] start = getPageStart(pageNumber);

        TextView screen;
        StringBuilder text = new StringBuilder();
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("AR"));

        int startSurah = start[0];
        int startAyah = start[1];
        int surahCounter = 0;
        int ayahCounter = 0;
        int currentSurah = startSurah + surahCounter;
        int currentAyah = startAyah + 1;
        boolean first;
        boolean last;

        try {
            while (surahCounter < 5) {
                first = false;
                last = false;

                surah = surahs.getJSONObject(currentSurah);
                ayahs = surah.getJSONArray("ayahs");
                ayah = ayahs.getJSONObject(startAyah + ayahCounter);

                if (currentAyah == 1)
                    first = true;
                else if (currentAyah == ayahs.length())
                    last = true;

                if (ayah.getInt("page") == pageNumber) {
                    if (first) {
                        screen = screen();
                        screen.setText(text);
                        mainLinear.addView(screen);

                        text.setLength(0);
                        TextView nameScreen = surahName(surah.getString("name"));
                        TextView basmalah = basmalah();
                        mainLinear.addView(nameScreen);
                        mainLinear.addView(basmalah);
                    }
                    text.append(ayah.getString("text"));
                    text.append(" ").append(reverse(nf.format(currentAyah))).append("\u06DD").append(" ");
                    //text.append(" ").append(nf.format(currentAyah)).append("\u06DD").append(" ");
                    ayahCounter++;
                    currentAyah++;
                }
                else
                    break;

                if (last) {
                    text.append(".\n");
                    surahCounter++;
                    startAyah = 0;
                    ayahCounter = 0;
                    currentSurah = startSurah + surahCounter;
                    currentAyah = 1;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e("myself", "problemo");
        }
        screen = screen();
        screen.setText(text);
        mainLinear.addView(screen);
    }

    private String reverse(String given) {
        int digits = given.length();
        if (digits == 1)
            return given;
        else {
            String temp = "";

            if (digits == 3)
                temp += given.charAt(2);

            temp += given.charAt(1);
            temp += given.charAt(0);
            return temp;
        }
    }

    private void setListeners() {
        binding.prev.setOnClickListener(v -> {
            if (currentPage > 1) {
                buildPage(--currentPage);
                Objects.requireNonNull(getSupportActionBar()).setTitle("رقم الصفحة " + currentPage);
                binding.scrollView.scrollTo(0, 0);
            }
        });
        binding.next.setOnClickListener(v -> {
            if (currentPage < Constants.QURAN_PAGES) {
                buildPage(++currentPage);
                Objects.requireNonNull(getSupportActionBar()).setTitle("رقم الصفحة " + currentPage);
                binding.scrollView.scrollTo(0, 0);
            }
        });
    }

    private TextView surahName(String name) {
        TextView nameScreen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameScreen.setLayoutParams(screenParams);
        nameScreen.setPadding(0, 0, 0, 15);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            nameScreen.setTextColor(getResources().getColor(R.color.secondary, getTheme()));
        else
            nameScreen.setTextColor(getResources().getColor(R.color.secondary));
        nameScreen.setGravity(Gravity.CENTER);
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
        nameScreen.setGravity(Gravity.CENTER);
        nameScreen.setPadding(0, 0, 0, 10);
        nameScreen.setTextSize(textSize);
        nameScreen.setTypeface(Typeface.DEFAULT_BOLD);
        //nameScreen.setTypeface(getResources().getFont(R.font.quran_surah1));
        nameScreen.setText("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ");

        return nameScreen;
    }

    private TextView screen() {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screen.setLayoutParams(screenParams);
        screen.setPadding(10, 10, 10, 10);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize);
        //screen.setTypeface(getResources().getFont(R.font.uthman_ver10));

        return screen;
    }

    private int getSize() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(getString(R.string.quran_text_size_key), 10);
    }

}
