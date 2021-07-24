package com.bassamalim.athkar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bassamalim.athkar.databinding.ActivityAthkarViewBinding;
import com.bassamalim.athkar.databinding.QuranViewBinding;

import java.util.Objects;

public class QuranView extends AppCompatActivity {

    QuranViewBinding binding;
    LinearLayout linear;
    private static final String TAG = "QuranView";
    String surah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = QuranViewBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        CharSequence title = intent.getCharSequenceExtra("title");

        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        linear = binding.linear;

        surah = intent.getStringExtra("key");

        binding.quranView.setText(surah);
    }

}
