package bassamalim.hidaya.activities

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityRadioClientBinding
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import bassamalim.hidaya.services.RadioService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RadioClient : AppCompatActivity() {

    private var binding: ActivityRadioClientBinding? = null
    private var remoteConfig: FirebaseRemoteConfig? = null
    private var mediaBrowser: MediaBrowserCompat? = null
    private var controller: MediaControllerCompat? = null
    private var tc: MediaControllerCompat.TransportControls? = null
    private var playBtn: ImageButton? = null // play/pause button
    private var link: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityRadioClientBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }
        playBtn = binding!!.radioPpBtn
        mediaBrowser = MediaBrowserCompat(
            this, ComponentName(
                this,
                RadioService::class.java
            ), connectionCallbacks, null
        ) // optional Bundle
        getLinkAndEnable()
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser!!.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        if (MediaControllerCompat.getMediaController(this@RadioClient) != null) {
            MediaControllerCompat.getMediaController(this@RadioClient)
                .unregisterCallback(controllerCallback)
        }
        mediaBrowser!!.disconnect()
    }

    private fun getLinkAndEnable() {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig!!.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                link = remoteConfig!!.getString("quran_radio_url")
                Log.d(Global.TAG, "Config params updated")
                Log.d(Global.TAG, "Quran Radio URL: $link")
                enableControls()
            } else Log.d(Global.TAG, "Fetch failed")
        }
    }

    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Get the token for the MediaSession
            val token: MediaSessionCompat.Token = mediaBrowser!!.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(
                this@RadioClient, token
            )

            // Save the controller
            MediaControllerCompat.setMediaController(this@RadioClient, mediaController)
            controller = MediaControllerCompat.getMediaController(this@RadioClient)
            tc = controller!!.transportControls

            // Finish building the UI
            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in RadioClient")
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
            disableControls()
        }

        override fun onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in RadioClient")
            // The Service has refused our connection
            disableControls()
        }
    }

    private var controllerCallback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            // To change the metadata inside the app when the user changes it from the notification
            //updateMetadata(metadata);
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            // To change the playback state inside the app when the user changes it
            // from the notification
            updatePbState(state)
        }
    }

    private fun buildTransportControls() {
        //enableControls();

        // Display the initial state
        updatePbState(controller!!.playbackState)

        // Register a Callback to stay in sync
        controller!!.registerCallback(controllerCallback)
    }

    private fun updatePbState(state: PlaybackStateCompat) {
        val currentState: Int = state.state
        updateButton(currentState == PlaybackStateCompat.STATE_PLAYING)
    }

    private fun updateButton(playing: Boolean) {
        if (playing) playBtn!!.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_player_pause, theme
            )
        ) else playBtn!!.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_player_play, theme
            )
        )
    }

    private fun enableControls() {
        // Attach a listeners to the buttons
        playBtn!!.setOnClickListener {
            // Since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            val pbState: Int = controller!!.playbackState.state
            if (pbState == PlaybackStateCompat.STATE_PLAYING) tc!!.pause() else tc!!.playFromMediaId(
                link,
                null
            )
        }
    }

    private fun disableControls() {
        playBtn!!.setOnClickListener(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        mediaBrowser = null
    }
}