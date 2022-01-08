package bassamalim.hidaya.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import bassamalim.hidaya.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
    }

    private void setListeners() {
        binding.agreed.setOnClickListener(view -> {
            Intent intent = new Intent(this, Splash.class);
            startActivity(intent);
            finish();
        });
        binding.rejected.setOnClickListener(view -> launchAnyWay());
    }

    private void launchAnyWay() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("located", false);
        startActivity(intent);
        finish();
    }
}