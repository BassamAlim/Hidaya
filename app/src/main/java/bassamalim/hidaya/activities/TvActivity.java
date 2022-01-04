package bassamalim.hidaya.activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.MediaController;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import bassamalim.hidaya.databinding.ActivityTvBinding;
import bassamalim.hidaya.other.Constants;
import bassamalim.hidaya.replacements.FSMediaController;

public class TvActivity extends AppCompatActivity {

    private ActivityTvBinding binding;
    private static Uri uri = Uri.parse("");
    private FirebaseRemoteConfig remoteConfig;
    private String makkah_url;
    private String madina_url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTvBinding.inflate(getLayoutInflater());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(binding.getRoot());

        getLinks();

        MediaController controller = new FSMediaController(this);
        controller.setAnchorView(binding.screen);
        binding.screen.setMediaController(controller);
    }

    private void getLinks() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        makkah_url = remoteConfig.getString("makkah_url");
                        madina_url = remoteConfig.getString("madina_url");

                        Log.d(Constants.TAG, "Config params updated");
                        Log.d(Constants.TAG, "Makkah URL: " + makkah_url);
                        Log.d(Constants.TAG, "Madina URL: " + madina_url);

                        setListeners();
                        enableButtons();
                    }
                    else
                        Log.d(Constants.TAG, "Fetch failed");
                });
    }

    private void setListeners() {
        binding.quran.setOnClickListener(v -> show(makkah_url));
        binding.sunnah.setOnClickListener(v -> show(madina_url));
    }

    private void enableButtons() {
        binding.quran.setEnabled(true);
        binding.sunnah.setEnabled(true);
    }

    public void show(String giverUri) {
        uri = Uri.parse(giverUri);
        binding.screen.setVideoURI(uri);
        binding.screen.start();
    }

    public static Uri getUrl() {
        return uri;
    }
}

/*
Other Links:-
quran:
uri = Uri.parse("https://youtu.be/jbUI2d6RwE8");
            viewIntent = new Intent("android.intent.action.VIEW", uri);
            startActivity(viewIntent);

sunnah:
uri = Uri.parse("https://www.youtube.com/watch?v=DhlO2YyVSng");
            viewIntent = new Intent("android.intent.action.VIEW", uri);
            startActivity(viewIntent);
 */