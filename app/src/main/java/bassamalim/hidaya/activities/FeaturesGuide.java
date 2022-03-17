package bassamalim.hidaya.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import bassamalim.hidaya.R;
import bassamalim.hidaya.other.Utils;

public class FeaturesGuide extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_features_guide);
    }

}