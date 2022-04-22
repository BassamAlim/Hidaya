package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import bassamalim.hidaya.databinding.ActivityQuizLobbyBinding;
import bassamalim.hidaya.other.Utils;

public class QuizLobbyActivity extends AppCompatActivity {

    private ActivityQuizLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityQuizLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        setListeners();
    }

    private void setListeners() {
        binding.startQuiz.setOnClickListener(view -> {
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
        });
    }

}