package com.bassamalim.athkar.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.Utils;
import com.bassamalim.athkar.databinding.QuranViewBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

public class QuranView extends AppCompatActivity {

    private QuranViewBinding binding;
    private LinearLayout mainLinear;
    private JSONArray jsonArray;
    private int currentPage;
    private int textSize;
    boolean swapped = false;
    private float x1;
    static final int MIN_DISTANCE = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = QuranViewBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

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

        setListeners();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        this.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(swapped) {
            swapped = false;
            return super.onTouchEvent(event);
        }
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                float x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    swapped = true;
                    if (x2 > x1)    // Left to Right
                        binding.next.performClick();
                    else    // Right to left
                        binding.prev.performClick();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void setupJson() {
        String jsonString = Utils.getJsonFromAssets(this, "hafs_smart_v8.json");
        try {
            assert jsonString != null;
            jsonArray = new JSONArray(jsonString);
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

    public int getPageStart(int pageNumber) {
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

    public void buildPage(int pageNumber) {
        setSupportActionBar(binding.numberBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        String temp = "رقم الصفحة " + currentPage;
        binding.pageNumber.setText(temp);

        //binding.pagedNumber.setText(temp);

        mainLinear.removeAllViews();

        int start = getPageStart(pageNumber);

        TextView screen;
        StringBuilder text = new StringBuilder();

        int counter = start;
        try {
            int currentSurah = jsonArray.getJSONObject(counter).getInt("sura_no");
            do {
                JSONObject ayah = jsonArray.getJSONObject(counter);
                int surahNum = ayah.getInt("sura_no");
                int ayahNum = ayah.getInt("aya_no");

                if (currentSurah != surahNum) {
                    text.append(".");
                    currentSurah = surahNum;
                }

                if (ayahNum == 1) {
                    if (text.length() > 0) {
                        screen = screen();
                        screen.setText(text);
                        mainLinear.addView(screen);
                    }
                    text.setLength(0);
                    TextView nameScreen = surahName(ayah.getString("sura_name_ar"));
                    TextView basmalah = basmalah();
                    mainLinear.addView(nameScreen);
                    mainLinear.addView(basmalah);
                }

                text.append(ayah.getString("aya_text")).append(" ");

            } while (jsonArray.getJSONObject(++counter).getInt("page") == pageNumber);
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "trouble in building page");
        }

        screen = screen();
        screen.setText(text);
        mainLinear.addView(screen);
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
        screenParams.bottomMargin = 20;
        nameScreen.setLayoutParams(screenParams);
        nameScreen.setGravity(Gravity.CENTER);
        nameScreen.setTextIsSelectable(true);
        nameScreen.setBackground(AppCompatResources.getDrawable(this, R.drawable.surah_header));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            nameScreen.setTextColor(getResources().getColor(R.color.secondary, getTheme()));
        else
            nameScreen.setTextColor(getResources().getColor(R.color.secondary));
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
        nameScreen.setTextIsSelectable(true);
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
        screen.setPadding(0, 0, 0, 30);
        screen.setGravity(Gravity.CENTER);
        screen.setTextIsSelectable(true);
        screen.setTextSize(textSize);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "hafs_smart_08.ttf");
        screen.setTypeface(typeface);

        return screen;
    }

    private int getSize() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(getString(R.string.quran_text_size_key), 30);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
