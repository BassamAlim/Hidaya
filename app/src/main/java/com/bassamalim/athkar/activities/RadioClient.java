package com.bassamalim.athkar.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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
import com.bassamalim.athkar.services.RadioService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class RadioClient extends AppCompatActivity {

    private ActivityRadioPlayerBinding binding;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat controller;
    private MediaMetadataCompat mediaMetadata;
    private PlaybackStateCompat pbState;
    private TextView progressScreen;
    private TextView durationScreen;
    private TextView surahNamescreen;
    private ImageButton playPause;
    private ImageButton nextBtn;
    private ImageButton prevBtn;
    private ImageButton forwardBtn;
    private ImageButton rewindBtn;
    private SeekBar seekBar;
    private String action;
    private int reciter;
    private int versionIndex;
    private ArrayList<String> surahNames;
    private String reciterName;
    private RecitationVersion version;
    private int surahIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRadioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        action = getIntent().getAction();

        getIntentData();

        setupJson();

        initViews();

        // Create MediaBrowserServiceCompat
        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this,
                RadioService.class), connectionCallbacks, null); // optional Bundle
    }

    @Override
    public void onStart() {
        super.onStart();
        mediaBrowser.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(Constants.TAG, "in onStop of radio client");
        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(RadioClient.this) != null) {
            MediaControllerCompat.getMediaController(RadioClient.this)
                    .unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            // Get the token for the MediaSession
            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

            // Create a MediaControllerCompat
            MediaControllerCompat mediaController = null;
            try {
                mediaController = new MediaControllerCompat(RadioClient.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // Save the controller
            MediaControllerCompat.setMediaController(RadioClient.this, mediaController);
            controller = MediaControllerCompat.getMediaController(RadioClient.this);

            // Finish building the UI
            buildTransportControls();

            getIntentData();

            if (action.equals("start")) {
                // Pass media data
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("surah_names", surahNames);
                bundle.putInt("reciter_index", reciter);
                bundle.putString("reciter_name", reciterName);
                bundle.putSerializable("version", version);

                // Start Playback
                controller.getTransportControls()
                        .playFromMediaId(String.valueOf(surahIndex), bundle);
            }
        }

        @Override
        public void onConnectionSuspended() {
            Log.e(Constants.TAG, "Connection suspended in RadioClient");
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
            disableControls();
        }

        @Override
        public void onConnectionFailed() {
            Log.e(Constants.TAG, "Connection failed in RadioClient");
            // The Service has refused our connection
        }
    };

    private void getIntentData() {
        Intent intent = getIntent();
        reciter = intent.getIntExtra("reciter_index", 0);
        versionIndex = intent.getIntExtra("version", 0);
        surahIndex = intent.getIntExtra("surah_index", 0);
        surahNames = intent.getStringArrayListExtra("surah_names");
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

            version = new RecitationVersion(versionIndex, v.getString("server"),
                    v.getString("rewaya"), v.getString("count"),
                    v.getString("suras"), null);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        surahNamescreen = binding.surahNamescreen;
        seekBar = binding.seekbar;
        durationScreen = binding.durationScreen;
        progressScreen = binding.progressScreen;
        playPause = binding.playPause;
        nextBtn = binding.nextTrack;
        prevBtn = binding.previousTrack;
        forwardBtn = binding.fastForward;
        rewindBtn = binding.rewind;

        surahNamescreen.setText(surahNames.get(surahIndex));
        binding.reciterNamescreen.setText(reciterName);
        binding.versionNamescreen.setText(version.getRewaya());
    }

    private void updateButton(boolean playing) {
        if (playing)
            playPause.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_player_pause, getTheme()));
        else
            playPause.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_player_play, getTheme()));
    }

    private void enableControls() {
        // Attach a listeners to the buttons
        playPause.setOnClickListener(v -> {
            // Since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            int pbState = controller.getPlaybackState().getState();

            if (pbState == PlaybackStateCompat.STATE_PLAYING)
                controller.getTransportControls().pause();
            else
                controller.getTransportControls().play();
        });
        nextBtn.setOnClickListener(v -> controller.getTransportControls().skipToNext());
        prevBtn.setOnClickListener(v -> controller.getTransportControls().skipToPrevious());
        forwardBtn.setOnClickListener(v -> controller.getTransportControls().fastForward());
        rewindBtn.setOnClickListener(v -> controller.getTransportControls().rewind());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    controller.getTransportControls().seekTo(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void disableControls() {
        // Attach a listeners to the buttons
        playPause.setOnClickListener(null);
        nextBtn.setOnClickListener(null);
        prevBtn.setOnClickListener(null);
        forwardBtn.setOnClickListener(null);
        rewindBtn.setOnClickListener(null);
        seekBar.setOnSeekBarChangeListener(null);
    }

    private void buildTransportControls() {
        enableControls();

        // Display the initial state
        mediaMetadata = controller.getMetadata();
        pbState = controller.getPlaybackState();
        updatePbState(pbState);
        updateMetadata();

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback);
    }

    MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {

            mediaMetadata = metadata;
            // To change the metadata inside the app when the user changes it from the notification
            updateMetadata();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            pbState = state;
            // To change the playback state inside the app when the user changes it
            // from the notification
            updatePbState(state);
        }
    };

    private void updateMetadata() {
        surahIndex = (int) mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);
        surahNamescreen.setText(surahNames.get(surahIndex));

        int duration = (int) mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        durationScreen.setText(formatTime(duration));
        seekBar.setMax(duration);
    }

    private void updatePbState(PlaybackStateCompat state) {
        seekBar.setProgress((int) state.getPosition());
        progressScreen.setText(formatTime((int) state.getPosition()));

        int currentState = state.getState();
        switch (currentState) {
            case PlaybackStateCompat.STATE_PLAYING: {
                updateButton(true);
                progressScreen.setText(formatTime((int) state.getPosition()));
                break;
            }
            case PlaybackStateCompat.STATE_NONE: {}
            case PlaybackStateCompat.STATE_STOPPED: {}
            case PlaybackStateCompat.STATE_PAUSED: {}
            case PlaybackStateCompat.STATE_FAST_FORWARDING: {}
            case PlaybackStateCompat.STATE_REWINDING: {}
            case PlaybackStateCompat.STATE_BUFFERING: {}
            case PlaybackStateCompat.STATE_ERROR: {}
            case PlaybackStateCompat.STATE_CONNECTING: {}
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS: {}
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT: {}
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM: {
                updateButton(false);
            }
        }
    }

    private String formatTime(int time) {
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
        mediaBrowser = null;
    }
}