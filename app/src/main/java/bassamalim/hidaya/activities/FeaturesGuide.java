package bassamalim.hidaya.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import bassamalim.hidaya.R;
import bassamalim.hidaya.other.Utils;

public class FeaturesGuide extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_features_guide);
        findViewById(android.R.id.home).setOnClickListener(v -> onBackPressed());
    }

}