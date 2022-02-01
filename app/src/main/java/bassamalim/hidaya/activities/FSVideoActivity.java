package bassamalim.hidaya.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import bassamalim.hidaya.replacements.FSMediaController;
import bassamalim.hidaya.R;

public class FSVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_videoview);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        VideoView videoView = findViewById(R.id.fullscreen);

        String fullScreen =  getIntent().getStringExtra("fullScreenInd");
        if ("y".equals(fullScreen))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else {
            Intent intent = new Intent(getApplicationContext(), TvActivity.class);
            startActivity(intent);
        }

        Uri videoUri = TvActivity.getUrl();
        videoView.setVideoURI(videoUri);

        MediaController mediaController = new FSMediaController(this);
        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);
        videoView.start();
    }

}
