package bassamalim.hidaya.activities;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.ActivityRadioClientBinding;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.services.RadioService;

public class RadioClient extends AppCompatActivity {

    private ActivityRadioClientBinding binding;
    private SharedPreferences pref;
    private FirebaseRemoteConfig remoteConfig;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat controller;
    private MediaControllerCompat.TransportControls tc;
    private ImageButton ppBtn;    // play/pause button
    private String theme;
    private String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityRadioClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        initViews();

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this,
                RadioService.class), connectionCallbacks, null); // optional Bundle

        getLinkAndEnable();
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
        if (MediaControllerCompat.getMediaController(RadioClient.this) != null) {
            MediaControllerCompat.getMediaController(RadioClient.this)
                    .unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
    }

    private void initViews() {
        theme = pref.getString(getString(R.string.theme_key), "ThemeM");

        ppBtn = binding.radioPpBtn;
    }

    private void getLinkAndEnable() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                link = remoteConfig.getString("quran_radio_url");

                Log.d(Global.TAG, "Config params updated");
                Log.d(Global.TAG, "Quran Radio URL: " + link);

                enableControls();
            }
            else
                Log.d(Global.TAG, "Fetch failed");
        });
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            // Get the token for the MediaSession
            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

            // Create a MediaControllerCompat
            MediaControllerCompat mediaController = new MediaControllerCompat(
                    RadioClient.this, token);

            // Save the controller
            MediaControllerCompat.setMediaController(RadioClient.this, mediaController);
            controller = MediaControllerCompat.getMediaController(RadioClient.this);
            tc = controller.getTransportControls();

            // Finish building the UI
            buildTransportControls();
        }

        @Override
        public void onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in RadioClient");
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
            disableControls();
        }

        @Override
        public void onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in RadioClient");
            // The Service has refused our connection
            disableControls();
        }
    };

    MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            // To change the metadata inside the app when the user changes it from the notification
            //updateMetadata(metadata);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            // To change the playback state inside the app when the user changes it
            // from the notification
            updatePbState(state);
        }
    };

    private void buildTransportControls() {
        //enableControls();

        // Display the initial state
        updatePbState(controller.getPlaybackState());

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback);
    }

    private void updatePbState(PlaybackStateCompat state) {
        int currentState = state.getState();
        updateButton(currentState == PlaybackStateCompat.STATE_PLAYING);
    }

    private void updateButton(boolean playing) {
        if (theme.equals("ThemeM")) {
            if (playing)
                ppBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_player_pause, getTheme()));
            else
                ppBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_player_play, getTheme()));
        }
        else {
            if (playing)
                ppBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_player_pause_l, getTheme()));
            else
                ppBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_player_play_l, getTheme()));
        }
    }

    private void enableControls() {
        // Attach a listeners to the buttons
        ppBtn.setOnClickListener(v -> {
            // Since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            int pbState = controller.getPlaybackState().getState();

            if (pbState == PlaybackStateCompat.STATE_PLAYING)
                tc.pause();
            else
                tc.playFromMediaId(link, null);
        });
    }

    private void disableControls() {
        ppBtn.setOnClickListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        mediaBrowser = null;
    }
}