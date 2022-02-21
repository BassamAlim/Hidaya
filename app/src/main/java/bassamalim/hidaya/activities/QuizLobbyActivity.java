package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import bassamalim.hidaya.databinding.ActivityQuizLobbyBinding;
import bassamalim.hidaya.other.Util;

public class QuizLobbyActivity extends AppCompatActivity {

    private ActivityQuizLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.onActivityCreateSetTheme(this);
        binding = ActivityQuizLobbyBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        setListeners();
    }

    private void setListeners() {
        binding.startQuiz.setOnClickListener(view -> {
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
        });
    }

}