package bassamalim.hidaya.activities;

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
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Locale;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.TelawatVersionsDB;
import bassamalim.hidaya.databinding.ActivityTelawatPlayerBinding;
import bassamalim.hidaya.models.ReciterCard;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.services.TelawatService;

public class TelawatClient extends AppCompatActivity {

    private ActivityTelawatPlayerBinding binding;
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
    private int reciterId;
    private String rewaya;
    private ArrayList<String> surahNames;
    private String reciterName;
    private ReciterCard.RecitationVersion version;
    private int surahId;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTelawatPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        action = getIntent().getAction();

        getIntentData();

        getData();

        initViews();

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this,
                TelawatService.class), connectionCallbacks, null); // optional Bundle
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
        if (surahNames == null)
            getSurahNames();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(Global.TAG, "in onStop of TelawatClient");
        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(TelawatClient.this) != null) {
            MediaControllerCompat.getMediaController(TelawatClient.this)
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
                mediaController = new MediaControllerCompat(TelawatClient.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            // Save the controller
            MediaControllerCompat.setMediaController(TelawatClient.this, mediaController);
            controller = MediaControllerCompat.getMediaController(TelawatClient.this);

            // Finish building the UI
            buildTransportControls();

            getIntentData();

            if (controller.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING
                    && action.equals("start")) {
                // Pass media data
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("surah_names", surahNames);
                bundle.putInt("reciter_id", reciterId);
                bundle.putString("reciter_name", reciterName);
                bundle.putSerializable("version", version);

                // Start Playback
                controller.getTransportControls().playFromMediaId(String.valueOf(surahId), bundle);
            }
        }

        @Override
        public void onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in TelawatClient");
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
            disableControls();
        }

        @Override
        public void onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in TelawatClient");
            // The Service has refused our connection
        }
    };

    private void getIntentData() {
        Intent intent = getIntent();
        reciterId = intent.getIntExtra("reciter_id", 0);
        rewaya = intent.getStringExtra("rewaya");
        surahId = intent.getIntExtra("surah_id", 0);
        getSurahNames();
    }

    private void getData() {
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        reciterName = db.telawatRecitersDao().getNames().get(reciterId);

        TelawatVersionsDB telawa = db.telawatVersionsDao().getVersion(reciterId, rewaya);
        version = new ReciterCard.RecitationVersion(telawa.getUrl(), telawa.getRewaya(),
                telawa.getCount(), telawa.getSuras(), null);

        if (surahNames == null)
            getSurahNames();
    }

    private void getSurahNames() {
        surahNames = (ArrayList<String>) db.suraDao().getNames();
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

        surahNamescreen.setText(surahNames.get(surahId));
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
        surahId = (int) mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);
        surahNamescreen.setText(surahNames.get(surahId));

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