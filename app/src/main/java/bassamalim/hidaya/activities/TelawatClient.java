package bassamalim.hidaya.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.TelawatVersionsDB;
import bassamalim.hidaya.databinding.ActivityTelawatPlayerBinding;
import bassamalim.hidaya.models.ReciterCard;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.services.TelawatService;

public class TelawatClient extends AppCompatActivity {

    private ActivityTelawatPlayerBinding binding;
    private AppDatabase db;
    private SharedPreferences pref;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat controller;
    private MediaControllerCompat.TransportControls tc;
    private TextView surahNamescreen;
    private SeekBar seekBar;
    private TextView progressScreen;
    private TextView durationScreen;
    private ImageButton playPause;
    private ImageButton nextBtn;
    private ImageButton prevBtn;
    private ImageButton forwardBtn;
    private ImageButton rewindBtn;
    private ImageButton repeatBtn;
    private ImageButton shuffleBtn;
    private String action;
    private String mediaId;
    private int reciterId;
    private int versionId;
    private int surahIndex;
    private String reciterName;
    private ReciterCard.RecitationVersion version;
    private ArrayList<String> surahNames;
    private int repeat;
    private int shuffle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTelawatPlayerBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "HidayaDB").createFromAsset("databases/HidayaDB.db").allowMainThreadQueries()
                .build();

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        getData();

        retrieveState();

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this,
                TelawatService.class), connectionCallbacks, null); // optional Bundle

        initViews();

        setListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(Global.TAG, "In OnNewIntent");

        getData();

        sendPlayRequest();
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
            tc = controller.getTransportControls();

            // Finish building the UI
            buildTransportControls();

            getData();

            if (!action.equals("back"))
                sendPlayRequest();
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
            disableControls();
        }
    };

    private void getData() {
        Intent intent = getIntent();
        action = intent.getAction();

        if (action.equals("continue"))
            mediaId = pref.getString("last_played_media_id", "");
        else
            mediaId = intent.getStringExtra("media_id");

        reciterId = Integer.parseInt(mediaId.substring(0, 3));
        versionId = Integer.parseInt(mediaId.substring(3, 5));
        surahIndex = Integer.parseInt(mediaId.substring(5));

        reciterName = db.telawatRecitersDao().getNames().get(reciterId);

        TelawatVersionsDB telawa = db.telawatVersionsDao().getVersion(reciterId, versionId);
        version = new ReciterCard.RecitationVersion(versionId, telawa.getUrl(), telawa.getRewaya(),
                telawa.getCount(), telawa.getSuras(), null);

        surahNames = (ArrayList<String>) db.suraDao().getNames();
    }

    private void retrieveState() {
        repeat = pref.getInt("telawat_repeat_mode", 0);
        shuffle = pref.getInt("telawat_shuffle_mode", 0);
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
        repeatBtn = binding.repeat;
        shuffleBtn = binding.shuffle;

        surahNamescreen.setText(surahNames.get(surahIndex));
        binding.reciterNamescreen.setText(reciterName);
        binding.versionNamescreen.setText(version.getRewaya());

        if (repeat == PlaybackStateCompat.REPEAT_MODE_ONE)
            repeatBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ripple_b, getTheme()));
        if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL)
            shuffleBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ripple_b, getTheme()));
    }

    private void setListeners() {
        binding.back.setOnClickListener(v -> onBackPressed());
        repeatBtn.setOnClickListener(v -> {
            if (repeat == PlaybackStateCompat.REPEAT_MODE_NONE) {
                repeat = PlaybackStateCompat.REPEAT_MODE_ONE;
                tc.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("telawat_repeat_mode", repeat);
                editor.apply();

                repeatBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ripple_b, getTheme()));
            }
            else if (repeat == PlaybackStateCompat.REPEAT_MODE_ONE) {
                repeat = PlaybackStateCompat.REPEAT_MODE_NONE;
                tc.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("telawat_repeat_mode", repeat);
                editor.apply();

                repeatBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ripple, getTheme()));
            }
        });
        shuffleBtn.setOnClickListener(v -> {
            if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                shuffle = PlaybackStateCompat.SHUFFLE_MODE_ALL;
                tc.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("telawat_shuffle_mode", shuffle);
                editor.apply();

                shuffleBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ripple_b, getTheme()));
            }
            else if (shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL){
                shuffle = PlaybackStateCompat.SHUFFLE_MODE_NONE;
                tc.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("telawat_shuffle_mode", shuffle);
                editor.apply();

                shuffleBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ripple, getTheme()));
            }
        });
    }

    private void sendPlayRequest() {
        // Pass media data
        Bundle bundle = new Bundle();
        bundle.putString("play_type", action);
        bundle.putString("reciter_name", reciterName);
        bundle.putSerializable("version", version);
        bundle.putStringArrayList("surah_names", surahNames);

        // Start Playback
        tc.playFromMediaId(mediaId, bundle);
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
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    tc.seekTo(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        playPause.setOnClickListener(v -> {
            // Since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            int pbState = controller.getPlaybackState().getState();

            if (pbState == PlaybackStateCompat.STATE_PLAYING)
                tc.pause();
            else
                tc.play();
        });
        nextBtn.setOnClickListener(v -> tc.skipToNext());
        prevBtn.setOnClickListener(v -> tc.skipToPrevious());
        forwardBtn.setOnClickListener(v -> tc.fastForward());
        rewindBtn.setOnClickListener(v -> tc.rewind());
    }

    private void disableControls() {
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
        updatePbState(controller.getPlaybackState());
        updateMetadata(controller.getMetadata());

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback);
    }

    MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            // To change the metadata inside the app when the user changes it from the notification
            updateMetadata(metadata);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            // To change the playback state inside the app when the user changes it
            // from the notification
            updatePbState(state);
        }
    };

    private void updateMetadata(MediaMetadataCompat metadata) {
        surahIndex = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);
        surahNamescreen.setText(surahNames.get(surahIndex));

        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
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
    public void onBackPressed() {
        super.onBackPressed();

        if (isTaskRoot()) {
            Intent intent = new Intent(this, TelawatSurahsActivity.class);
            intent.putExtra("reciter_id", reciterId);
            intent.putExtra("version_id", versionId);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        mediaBrowser = null;
    }
}