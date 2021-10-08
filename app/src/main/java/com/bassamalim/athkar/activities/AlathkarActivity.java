package com.bassamalim.athkar.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.helpers.Utils;
import com.bassamalim.athkar.databinding.AlathkarViewBinding;
import com.bassamalim.athkar.models.Thikr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class AlathkarActivity extends AppCompatActivity {

    private AlathkarViewBinding binding;
    private int textSize;
    private ArrayList<Thikr> thikrsList;
    private LinearLayout linear;
    private int category;
    private int index;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AlathkarViewBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        category = intent.getIntExtra("category", 0);
        index = intent.getIntExtra("index", 0);

        getThikrs();

        binding.thikrName.setText(name);

        linear = binding.linear;

        textSize = getSize();

        insert();
    }

    private void getThikrs() {
        String json = Utils.getJsonFromAssets(this, "alathkar.json");

        thikrsList = new ArrayList<>();

        try {
            JSONArray mainArray = new JSONArray(json);
            JSONObject cat = mainArray.getJSONObject(category);
            JSONArray alathkarArr = cat.getJSONArray("alathkar");
            JSONObject alathkar = alathkarArr.getJSONObject(index);
            JSONArray thikrsArr = alathkar.getJSONArray("thikrs");

            name = alathkar.getString("name_ar");

            for (int i = 0; i < thikrsArr.length(); i++) {
                JSONObject t = thikrsArr.getJSONObject(i);

                Thikr th = new Thikr(t.getString("title"), t.getString("thikr"),
                        t.getString("repetition"), t.getString("fadl"),
                        t.getString("reference"));

                thikrsList.add(th);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void insert() {
        for (int i = 0; i < thikrsList.size(); i++) {
            CardView card = card(thikrsList.get(i));
            linear.addView(card);
        }
    }

    private CardView card(Thikr model) {
        CardView card = new CardView(this);
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(20, 10, 20, 10);
        cardParams.gravity = Gravity.CENTER;
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(getResources().getColor(R.color.secondary));
        card.setRadius(40);
        card.setElevation(40);
        card.setPreventCornerOverlap(true);

        LinearLayout mainLL = new LinearLayout(this);
        LinearLayout.LayoutParams mainLLParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mainLL.setLayoutParams(mainLLParams);
        card.addView(mainLL);

        if (model.getRepetition().length() > 0) {
            LinearLayout repetitionLL = new LinearLayout(this);
            LinearLayout.LayoutParams horizontalLLParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            repetitionLL.setLayoutParams(horizontalLLParams);
            repetitionLL.setOrientation(LinearLayout.VERTICAL);
            repetitionLL.setGravity(Gravity.CENTER);
            mainLL.addView(repetitionLL);
            TextView repetition = repetitionScreen(model.getRepetition());
            repetitionLL.addView(repetition);

            mainLL.addView(separator(false, 10));
        }

        LinearLayout verticalLL = new LinearLayout(this);
        LinearLayout.LayoutParams verticalLLParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalLLParams.setMargins(10, 10, 10, 15);
        verticalLLParams.gravity = Gravity.CENTER;
        verticalLL.setLayoutParams(verticalLLParams);
        verticalLL.setOrientation(LinearLayout.VERTICAL);
        verticalLL.setGravity(Gravity.CENTER);
        mainLL.addView(verticalLL);

        if (model.getTitle().length() > 0) {
            TextView title = titleScreen(model.getTitle());
            verticalLL.addView(title);
        }
        if (model.getText().length() > 0) {
            TextView text = textScreen(model.getText());
            verticalLL.addView(text);
        }
        if (model.getFadl().length() > 0) {
            verticalLL.addView(separator(true, 7));

            TextView fadl = fadlScreen(model.getFadl());
            verticalLL.addView(fadl);
        }
        if (model.getReference().length() > 0) {
            verticalLL.addView(separator(true, 7));

            TextView reference = referenceScreen(model.getReference());
            verticalLL.addView(reference);
        }

        return card;
    }

    private TextView titleScreen(String title) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            screen.setTextColor(getResources().getColor(R.color.white, getTheme()));
        else
            screen.setTextColor(getResources().getColor(R.color.white));
        screen.setPadding(10, 0, 10, 0);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize);
        screen.setTypeface(Typeface.DEFAULT_BOLD);

        screen.setText(title);
        return screen;
    }

    private TextView textScreen(String text) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            screen.setTextColor(getResources().getColor(R.color.white, getTheme()));
        else
            screen.setTextColor(getResources().getColor(R.color.white));
        screen.setPadding(10, 10, 10, 20);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize);

        screen.setText(text);
        return screen;
    }

    private TextView repetitionScreen(String repetition) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            screen.setTextColor(getResources().getColor(R.color.accent, getTheme()));
        else
            screen.setTextColor(getResources().getColor(R.color.accent));
        screen.setPadding(15, 0, 15, 0);
        screen.setMaxWidth(300);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize-5);

        screen.setText(repetition);
        return screen;
    }

    private TextView fadlScreen(String fadl) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            screen.setTextColor(getResources().getColor(R.color.accent, getTheme()));
        else
            screen.setTextColor(getResources().getColor(R.color.accent));
        screen.setPadding(10, 0, 10, 0);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize-8);

        screen.setText(fadl);
        return screen;
    }

    private TextView referenceScreen(String reference) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            screen.setTextColor(getResources().getColor(R.color.black, getTheme()));
        else
            screen.setTextColor(getResources().getColor(R.color.black));
        screen.setPadding(10, 0, 10, 0);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize-8);

        screen.setText(reference);
        return screen;
    }

    private View separator(boolean horizontal, int size) {
        View separator = new View(this);
        separator.setBackgroundColor(getResources().getColor(R.color.primary));
        LinearLayout.LayoutParams params;
        if (horizontal) {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, size);
        }
        else {
            params = new LinearLayout.LayoutParams(
                    size, LinearLayout.LayoutParams.MATCH_PARENT);
        }
        params.setMargins(0, 10, 0, 10);
        separator.setLayoutParams(params);
        return separator;
    }

    private int getSize() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(getString(R.string.alathkar_text_size_key), 25);
    }

}
