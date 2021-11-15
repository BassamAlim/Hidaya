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

    // UI
    private TextView progressScreen;
    private TextView durationScreen;
    private TextView surahNamescreen;
    private ImageButton playPause;
    private ImageButton nextBtn;
    private ImageButton prevBtn;
    private ImageButton forwardBtn;
    private ImageButton rewindBtn;
    private SeekBar seekBar;

    private int reciter;
    private int versionIndex;
    private ArrayList<String> surahNames;
    private String reciterName;
    private RecitationVersion version;
    private int surahIndex;
    private int currentState;
    private int position;
    private int duration;
    private boolean isPlaying = false;
    private MediaMetadataCompat mediaMetadata;
    private PlaybackStateCompat pbState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Constants.TAG, "in onCreate in RadioClient");
        super.onCreate(savedInstanceState);

        binding = ActivityRadioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentData();

        setupJson();

        initViews();

        // Create MediaBrowserServiceCompat
        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, RadioService.class), connectionCallbacks,
                null); // optional Bundle
    }

    private void getIntentData() {
        Intent intent = getIntent();
        reciter = intent.getIntExtra("reciter", 0);
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
            Log.e(Constants.TAG, "Problems in setupJson() in RadioActivity");
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
        if (playing) {
            playPause.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_player_pause, getTheme()));
            isPlaying = true;
        }
        else {
            playPause.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_player_play, getTheme()));
            isPlaying = false;
        }
    }

    @Override
    public void onStart() {
        Log.i(Constants.TAG, "in onStart in RadioClient");
        super.onStart();
        mediaBrowser.connect();
    }

    @Override
    public void onResume() {
        Log.i(Constants.TAG, "in onResume in RadioClient");
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop() {
        Log.i(Constants.TAG, "in onStop in RadioClient");
        super.onStop();
        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(RadioClient.this) != null) {
            MediaControllerCompat.getMediaController(RadioClient.this)
                    .unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
    }

    @Override
    protected void onPause() {
        Log.i(Constants.TAG, "in onPause in RadioClient");
        super.onPause();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            Log.i(Constants.TAG, "in onConnected of connectionCallbacks in RadioClient");
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

            // Check if its the first time
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                // Pass media data
                Bundle bundle = new Bundle();
                bundle.putInt("reciter", reciter);
                bundle.putSerializable("version", version);
                bundle.putStringArrayList("surah_names", surahNames);
                bundle.putString("reciter_name", reciterName);

                // Start Playback
                controller.getTransportControls()
                        .playFromMediaId(String.valueOf(surahIndex), bundle);
                Log.i(Constants.TAG, "the id was: " + surahIndex);
            }
        }

        @Override
        public void onConnectionSuspended() {
            Log.i(Constants.TAG, "in onConnectionSuspended in RadioClient");
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
        }

        @Override
        public void onConnectionFailed() {
            Log.i(Constants.TAG, "in onConnectionFailed in RadioClient");
            // The Service has refused our connection
        }
    };

    private void setListeners() {
        // Attach a listeners to the buttons
        playPause.setOnClickListener(v -> {
            Log.i(Constants.TAG, "in playPause listener in RadioClient");
            // Since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            int pbState = controller.getPlaybackState().getState();

            if (pbState == PlaybackStateCompat.STATE_PLAYING)
                controller.getTransportControls().pause();
            else
                controller.getTransportControls().play();
        });
        nextBtn.setOnClickListener(v -> {
            Log.i(Constants.TAG, "in nextButton listener in RadioClient");
            controller.getTransportControls().skipToNext();
        });
        prevBtn.setOnClickListener(v -> {
            Log.i(Constants.TAG, "in prevButton listener in RadioClient");
            controller.getTransportControls().skipToPrevious();
        });
        forwardBtn.setOnClickListener(v -> {
            Log.i(Constants.TAG, "in forwardButton listener in RadioClient");
            controller.getTransportControls().fastForward();
        });
        rewindBtn.setOnClickListener(v -> {
            Log.i(Constants.TAG, "in backwardButton listener in RadioClient");
            controller.getTransportControls().rewind();
        });
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

    private void buildTransportControls() {
        Log.i(Constants.TAG, "in buildTransportControls in RadioClient");

        setListeners();

        // Display the initial state
        mediaMetadata = controller.getMetadata();
        pbState = controller.getPlaybackState();
        updatePbState(pbState);
        if (mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER) != -1)
            updateMetadata();

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback);
    }

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.i(Constants.TAG, "in onMetadataChanged of controllerCallback in RadioClient");

            mediaMetadata = metadata;
            // To change the metadata inside the app when the user changes it from the notification
            updateMetadata();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.i(Constants.TAG,
                    "in onPlaybackStateChanged of controllerCallback in RadioClient");

            pbState = state;
            // To change the playback state inside the app when the user changes it
            // from the notification
            updatePbState(state);
        }
    };

    private void updateMetadata() {
        surahIndex = (int) mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);
        surahNamescreen.setText(surahNames.get(surahIndex));

        duration = (int) mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        durationScreen.setText(formatTime(duration));
        seekBar.setMax(duration);
    }

    private void updatePbState(PlaybackStateCompat state) {
        Log.i(Constants.TAG, "in updatePbState in RadioClient");
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

}