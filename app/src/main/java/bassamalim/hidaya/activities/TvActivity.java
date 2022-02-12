package bassamalim.hidaya.activities;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

import bassamalim.hidaya.databinding.ActivityTvBinding;

public class TvActivity extends YouTubeBaseActivity {

    private ActivityTvBinding binding;
    private final String makkah_url = "ca-8pgBoa-s";
    private final String madina_url = "gUC3TjCrwRw";
    private final String apiKey = "AIzaSyBndJVjigZ7MOmj1005ONLUsfFW7BfxZt0";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTvBinding.inflate(getLayoutInflater());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(binding.getRoot());

        initYtPlayer();
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
                Toast.makeText(getApplicationContext(), "فشل التشغيل",
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
