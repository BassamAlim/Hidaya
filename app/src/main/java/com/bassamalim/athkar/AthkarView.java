package com.bassamalim.athkar;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bassamalim.athkar.databinding.ActivityAthkarViewBinding;
import java.util.Objects;

public class AthkarView extends AppCompatActivity {

    ActivityAthkarViewBinding binding;
    LinearLayout linear;
    String[] thikrs = new String[15];
    private static final String TAG = "AthkarView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAthkarViewBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        CharSequence title = intent.getCharSequenceExtra("title");

        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        //getSupportActionBar().setTitle(title);    works two ... but NOT getActionBar()

        linear = binding.linear;

        thikrs = intent.getStringArrayExtra("key");

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
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screen.setLayoutParams(screenParams);
        screen.setTextColor(getResources().getColor(R.color.secondary, getTheme()));
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(25);
        screen.setTypeface(Typeface.DEFAULT_BOLD);

        return screen;
    }

    public View divider() {
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 8);
        dividerParams.setMargins(0,15,0,15);
        divider.setLayoutParams(dividerParams);
        divider.setVisibility(View.VISIBLE);
        divider.setBackgroundColor(getResources().getColor(R.color.secondary, getTheme()));

        return divider;
    }

}
