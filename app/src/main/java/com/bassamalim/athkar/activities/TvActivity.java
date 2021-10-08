package com.bassamalim.athkar.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bassamalim.athkar.other.FSMediaController;
import com.bassamalim.athkar.databinding.TvViewBinding;

public class TvActivity extends AppCompatActivity {

    private TvViewBinding binding;
    private static Uri uri = Uri.parse("");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = TvViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setListeners();

        MediaController controller = new FSMediaController(this);
        controller.setAnchorView(binding.screen);
        binding.screen.setMediaController(controller);
    }

    private void setListeners() {
        binding.quran.setOnClickListener(v -> show(
                "https://cdnamd-hls-globecast.akamaized.net/live/ramdisk/saudi_quran/" +
                        "hls1/saudi_quran-avc1_600000=4-mp4a_97200=2.m3u8"));

        binding.sunnah.setOnClickListener(v -> show(
                "https://cdnamd-hls-globecast.akamaized.net/live/ramdisk/saudi_sunnah/" +
                        "hls1/saudi_sunnah-avc1_600000=5-mp4a_97200=2.m3u8"));
    }

    public void show(String giverUri) {
        uri = Uri.parse(giverUri);
        binding.screen.setVideoURI(uri);
        binding.screen.start();
    }

    public static Uri getUri() {
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