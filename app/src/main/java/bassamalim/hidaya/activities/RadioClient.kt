package bassamalim.hidaya.activities

import android.content.ComponentName
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityRadioClientBinding
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.RadioService
import bassamalim.hidaya.utils.ActivityUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

@RequiresApi(Build.VERSION_CODES.O)
class RadioClient : AppCompatActivity() {

    private lateinit var binding: ActivityRadioClientBinding
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private lateinit var playPause: ImageButton // play/pause button
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)
        binding = ActivityRadioClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { onBackPressed() }

        playPause = binding.radioPpBtn

        getLinkAndConnect()
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser?.connect()
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
        mediaBrowser?.disconnect()
    }

    private fun getLinkAndConnect() {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                url = remoteConfig.getString("quran_radio_url")
                Log.i(Global.TAG, "Config params updated")
                Log.i(Global.TAG, "Quran Radio URL: $url")

                connect()
            }
            else Log.e(Global.TAG, "Fetch failed")
        }
    }

    private fun connect() {
        mediaBrowser = MediaBrowserCompat(
            this, ComponentName(this, RadioService::class.java),
            connectionCallbacks, null
        )
        mediaBrowser!!.connect()
    }

    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Get the token for the MediaSession
            val token = mediaBrowser!!.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(this@RadioClient, token)

            // Save the controller
            MediaControllerCompat.setMediaController(this@RadioClient, mediaController)
            controller = MediaControllerCompat.getMediaController(this@RadioClient)
            tc = controller.transportControls

            // just sending the static url to extract the dynamic url
            tc.playFromMediaId(url, null)

            // Finish building the UI
            buildTransportControls()

            enableControls()
        }

        override fun onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in RadioClient")
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
            playPause.setOnClickListener(null)
        }

        override fun onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in RadioClient")
            // The Service has refused our connection
            playPause.setOnClickListener(null)
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
        // Display the initial state
        updatePbState(controller.playbackState)

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback)
    }

    private fun updatePbState(state: PlaybackStateCompat) {
        updateButton(state.state)
    }

    private fun updateButton(state: Int) {
        when(state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                binding.bufferingCircle.visibility = View.GONE
                playPause.visibility = View.VISIBLE
                playPause.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_player_pause, theme)
                )
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                binding.bufferingCircle.visibility = View.GONE
                playPause.visibility = View.VISIBLE
                playPause.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_player_play, theme)
                )
            }
            PlaybackStateCompat.STATE_BUFFERING -> {
                playPause.visibility = View.GONE
                binding.bufferingCircle.visibility = View.VISIBLE
            }
        }
    }

    private fun enableControls() {
        updateButton(PlaybackStateCompat.STATE_PAUSED)
        // Attach a listeners to the buttons
        playPause.setOnClickListener {
            // Since this is a play/pause button, you'll need to test the current state
            // and choose the action accordingly
            val pbState = controller.playbackState.state

            if (pbState == PlaybackStateCompat.STATE_PLAYING) tc.pause()
            else {
                tc.play()
                updateButton(PlaybackStateCompat.STATE_BUFFERING)
            }
        }
    }

}