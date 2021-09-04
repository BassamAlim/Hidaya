package com.bassamalim.athkar.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.AlathkarViewBinding;
import java.util.Objects;

public class AlathkarView extends AppCompatActivity {

    private AlathkarViewBinding binding;
    private LinearLayout linear;
    private String[] thikrs = new String[15];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AlathkarViewBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        CharSequence title = intent.getCharSequenceExtra("title");

        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        binding.thikrName.setText(title);

        linear = binding.linear;

        thikrs = intent.getStringArrayExtra("thikrs");

        insert();
    }

    public void insert() {
        for (int i = 0; i < thikrs.length; i++) {
            TextView screen = screen();
            screen.setText(thikrs[i]);
            linear.addView(screen);
            if (i != thikrs.length-1)
                linear.addView(divider());
        }
    }

    public TextView screen() {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screen.setLayoutParams(screenParams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            screen.setTextColor(getResources().getColor(R.color.white, getTheme()));
        else
            screen.setTextColor(getResources().getColor(R.color.white));
        screen.setPadding(10, 0, 10, 0);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(getSize());
        screen.setTypeface(Typeface.DEFAULT_BOLD);

        return screen;
    }

    public View divider() {
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 8);
        dividerParams.setMargins(0,25,0,25);
        divider.setLayoutParams(dividerParams);
        divider.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            divider.setBackgroundColor(getResources().getColor(R.color.secondary, getTheme()));
        else
            divider.setBackgroundColor(getResources().getColor(R.color.secondary));

        return divider;
    }

    private int getSize() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(getString(R.string.alathkar_text_size_key), 25);
    }

}
