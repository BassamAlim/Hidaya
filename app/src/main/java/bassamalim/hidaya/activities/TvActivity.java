package bassamalim.hidaya.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.ActivityTvBinding;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;

public class TvActivity extends YouTubeBaseActivity {

    private ActivityTvBinding binding;
    private FirebaseRemoteConfig remoteConfig;
    private String apiKey;
    private String makkah_url;
    private String madina_url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        Utils.setLocale(this, null);
        binding = ActivityTvBinding.inflate(getLayoutInflater());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        getLinksAndInit();
    }

    private void getLinksAndInit() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                apiKey = remoteConfig.getString("yt_api_key");
                makkah_url = remoteConfig.getString("makkah_url");
                madina_url = remoteConfig.getString("madina_url");

                Log.d(Global.TAG, "Config params updated");
                Log.d(Global.TAG, "Makkah URL: " + makkah_url);
                Log.d(Global.TAG, "Madina URL: " + madina_url);

                initYtPlayer();
            }
            else
                Log.d(Global.TAG, "Fetch failed");
        });
    }

    private void initYtPlayer() {
        binding.ytPlayer.initialize(apiKey, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                YouTubePlayer youTubePlayer, boolean b) {
                setListeners(youTubePlayer);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                YouTubeInitializationResult
                                                        youTubeInitializationResult) {
                Toast.makeText(getApplicationContext(), getString(R.string.playback_failed),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setListeners(YouTubePlayer ytPlayer) {
        binding.quranBtn.setOnClickListener(v -> {
            ytPlayer.loadVideo(makkah_url);
            ytPlayer.play();
        });
        binding.sunnahBtn.setOnClickListener(v -> {
            ytPlayer.loadVideo(madina_url);
            ytPlayer.play();
        });
    }

}
