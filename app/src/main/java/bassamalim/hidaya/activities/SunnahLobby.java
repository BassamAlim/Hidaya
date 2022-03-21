package bassamalim.hidaya.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import bassamalim.hidaya.databinding.ActivitySunnahLobbyBinding;
import bassamalim.hidaya.other.Utils;

public class SunnahLobby extends AppCompatActivity {

    private ActivitySunnahLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivitySunnahLobbyBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        setListeners();
    }

    private void setListeners() {
        binding.bokhariCard.setOnClickListener(v -> {
            /*if downloaded >> open
            else >> ask to download*/

            Intent intent = new Intent(this, SunnahChaptersCollectionActivity.class);
            intent.putExtra("book_id", 0);
            intent.putExtra("book_title", "صحيح البخاري");
            startActivity(intent);
        });
    }

}