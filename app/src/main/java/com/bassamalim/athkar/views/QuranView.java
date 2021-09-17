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
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
import com.bassamalim.athkar.dialogs.TafseerDialog;
import com.bassamalim.athkar.models.Ayah;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class QuranView extends AppCompatActivity {

    private QuranViewBinding binding;
    private LinearLayout mainLinear;
    private JSONArray jsonArray;
    private JSONArray tafseerArray;
    private int currentPage;
    private int textSize;
    private boolean swapped = false;
    private float x1;
    private float y1;
    private ArrayList<Ayah> arr;

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
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        this.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int MIN_X_DISTANCE = 300;
        int MAX_Y_DISTANCE = 300;
        if(swapped) {
            swapped = false;
            return super.onTouchEvent(event);
        }
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float x2 = event.getX();
                float y2 = event.getY();
                float distanceX = Math.abs(x2 - x1);
                float distanceY = Math.abs(y2 - y1);
                if (distanceX > MIN_X_DISTANCE && distanceY < MAX_Y_DISTANCE) {
                    swapped = true;
                    if (x2 > x1)    // Right to left
                        previousPage();
                    else            // Left to Right
                        nextPage();
                }
                break;
        }
        return super.onTouchEvent(event);
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

    private void buildPage(int pageNumber) {
        setSupportActionBar(binding.numberBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        String temp = "رقم الصفحة " + currentPage;
        binding.pageNumber.setText(temp);

        mainLinear.removeAllViews();
        arr = new ArrayList<>();

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

                Ayah ayahModel = new Ayah(ayahText + " ", tafseer);

                if (ayahNum == 1) {
                    if (arr.size() > 0)
                        publish(arr);

                    TextView nameScreen = surahName(ayah.getString("sura_name_ar"));
                    TextView basmalah = basmalah();
                    mainLinear.addView(nameScreen);
                    mainLinear.addView(basmalah);
                }
                arr.add(ayahModel);

            } while (++counter != 6236 && jsonArray.getJSONObject(counter)
                    .getInt("page") == pageNumber);

            publish(arr);
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "trouble in building page");
        }
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
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    new TafseerDialog(list.get(finalI).getTafseer()).show(
                            getSupportFragmentManager(), TafseerDialog.TAG);
                }
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
        screen.setMovementMethod(LinkMovementMethod.getInstance());
        screen.setLinkTextColor(Color.WHITE);
        mainLinear.addView(screen);
        arr = new ArrayList<>();
    }

    private void previousPage() {
        if (currentPage < Constants.QURAN_PAGES) {
            buildPage(++currentPage);
            Objects.requireNonNull(getSupportActionBar()).setTitle("رقم الصفحة " + currentPage);
            binding.scrollView.scrollTo(0, 0);
        }
    }

    private void nextPage() {
        if (currentPage > 1) {
            buildPage(--currentPage);
            Objects.requireNonNull(getSupportActionBar()).setTitle("رقم الصفحة " + currentPage);
            binding.scrollView.scrollTo(0, 0);
        }
    }

    private TextView surahName(String name) {
        TextView nameScreen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.bottomMargin = 20;
        nameScreen.setLayoutParams(screenParams);
        nameScreen.setPadding(0, 10, 0, 10);
        nameScreen.setGravity(Gravity.CENTER);
        nameScreen.setTextIsSelectable(true);
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
        nameScreen.setTextIsSelectable(true);
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
        screen.setTextIsSelectable(true);
        screen.setTextSize(textSize);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "hafs_smart_08.ttf");
        screen.setTypeface(typeface);

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
