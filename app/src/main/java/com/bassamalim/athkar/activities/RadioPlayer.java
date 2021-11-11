package com.bassamalim.athkar.activities;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.ActivityRadioPlayerBinding;
import com.bassamalim.athkar.helpers.Utils;
import com.bassamalim.athkar.models.RecitationVersion;
import com.bassamalim.athkar.other.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class RadioPlayer extends AppCompatActivity {

    private ActivityRadioPlayerBinding binding;
    private int reciter;
    private int versionIndex;
    private ArrayList<String> surahNames;
    private String reciterName;
    private RecitationVersion version;
    private int current;
    private MediaPlayer player;
    private WifiManager.WifiLock wifiLock;
    private boolean isPaused = false;
    private SeekBar seekBar;
    private TextView progressScreen;
    private TextView durationScreen;
    private TextView surahNamescreen;
    private ImageButton playPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRadioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        reciter = intent.getIntExtra("reciter", 0);
        versionIndex = intent.getIntExtra("version", 0);
        int surah = intent.getIntExtra("surah", 0);
        surahNames = intent.getStringArrayListExtra("surah_names");

        setupJson();

        current = surah;

        initiate();

        play();
    }

    private void setupJson() {
        String json = Utils.getJsonFromAssets(this, "mp3quran.json");
        try {
            assert json != null;
            JSONArray arr = new JSONArray(json);
            JSONObject reciterObj = arr.getJSONObject(reciter);
            reciterName = reciterObj.getString("name");
            JSONArray versions = reciterObj.getJSONArray("versions");
            JSONObject v = versions.getJSONObject(versionIndex);

            version = new RecitationVersion(v.getString("server"),
                    v.getString("rewaya"), v.getString("count"),
                    v.getString("suras"), null);
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, "Problems in setupJson() in RadioActivity");
        }
    }

    private void initiate() {
        setViews();

        surahNamescreen.setText(surahNames.get(current));
        binding.reciterNamescreen.setText(reciterName);
        binding.versionNamescreen.setText(version.getRewaya());

        setListeners();

        preparePlayer();
    }

    private void setViews() {
        surahNamescreen = binding.surahNamescreen;
        seekBar = binding.seekbar;
        durationScreen = binding.durationScreen;
        progressScreen = binding.progressScreen;
        playPause = binding.playPause;
    }

    private void setListeners() {
        playPause.setOnClickListener(v -> {
            if (player.isPlaying())
                pause();
            else {
                if (isPaused)
                    resume();
                else
                   play();
            }
        });
        binding.next.setOnClickListener(v -> next());
        binding.previous.setOnClickListener(v -> previous());
        binding.forward.setOnClickListener(v -> {
            if (player.isPlaying())
                player.seekTo(player.getCurrentPosition()+10000);
        });
        binding.backward.setOnClickListener(v -> {
            if (player.isPlaying())
                player.seekTo(player.getCurrentPosition()-10000);
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player != null && fromUser)
                    player.seekTo(progress);
            }
        });
    }

    private void preparePlayer() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //mediaPlayer.setLooping(true);
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "myLock");
        wifiLock.acquire();
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    private void play() {
        surahNamescreen.setText(surahNames.get(current));

        String text = version.getServer() + "/" + formatNum(current+1) + ".mp3";
        Uri uri = Uri.parse(text);

        try {
            player.reset();
            player.setDataSource(getApplicationContext(), uri);
            player.prepareAsync();
            player.setOnPreparedListener(mp -> {
                player.start();
                updateButton(true);
                seekBar.setMax(player.getDuration());
                durationScreen.setText(formatTime(false));
                updateSeekbar();
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        player.setOnCompletionListener(mp -> next());
    }

    private void pause() {
        player.pause();
        updateButton(false);
    }

    private void resume() {
        player.start();
        updateButton(true);
    }

    private void next() {
        updateButton(false);
        do {
            current++;
        } while(current < 114 && !version.getSuras().contains("," + (current+1) + ","));

        if (current < 114)
            play();
    }

    private void previous() {
        updateButton(false);
        do {
            current--;
        } while(current >= 0 && !version.getSuras().contains("," + (current+1) + ","));

        if (current >= 0)
        play();
    }

    private void updateSeekbar() {
        seekBar.setProgress(player.getCurrentPosition());
        progressScreen.setText(formatTime(true));
        seekBar.postDelayed(runnable, 1000);
    }

    private final Runnable runnable = new Runnable() {
        public void run() {
            if (player != null && player.isPlaying())
                updateSeekbar();
        }
    };

    private void updateButton(boolean playing) {
        if (playing) {
            playPause.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_player_pause, getTheme()));
            isPaused = false;
        }
        else {
            playPause.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_player_play, getTheme()));
            isPaused = true;
        }
    }

    private String formatNum(int in) {
        String strIn = String.valueOf(in);
        String out = "";
        if (strIn.length() == 1)
            out += "00";
        else if (strIn.length() == 2)
            out += "0";
        out += strIn;
        return out;
    }

    private String formatTime(boolean progress) {
        int time;
        if (progress)
            time = player.getCurrentPosition();
        else
            time = player.getDuration();

        int hours = time / (60 * 60 * 1000) % 24;
        int minutes = time / (60 * 1000) % 60;
        int seconds = time / 1000 % 60;
        String hms = String.format(Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds);
        if (hms.startsWith("0")) {
            hms = hms.substring(1);
            if (hms.startsWith("0"))
                hms = hms.substring(2);
        }
        return hms;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        if (player != null) {
            player.release();
            player = null;
        }
        if (wifiLock != null)
            wifiLock.release();
    }
}