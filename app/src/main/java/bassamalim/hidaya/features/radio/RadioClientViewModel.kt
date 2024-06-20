package bassamalim.hidaya.features.radio

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.core.other.Global
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RadioClientViewModel @Inject constructor(
    private val app: Application,
    repo: RadioClientRepository
): AndroidViewModel(app) {

    private lateinit var activity: Activity
    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private val url = repo.getLink()

    private val _uiState = MutableStateFlow(RadioClientState())
    val uiState = _uiState.asStateFlow()

    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                // Get the token for the MediaSession
                val token = mediaBrowser!!.sessionToken

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(app, token)

                // Save the controller
                MediaControllerCompat.setMediaController(activity, mediaController)
                controller = MediaControllerCompat.getMediaController(activity)
                tc = controller.transportControls

                // just sending the static url to extract the dynamic url
                tc.playFromMediaId(url, null)

                // Finish building the UI
                buildTransportControls()
            }

            override fun onConnectionSuspended() {
                Log.e(Global.TAG, "Connection suspended in RadioClient")
                // The Service has crashed.
                // Disable transport controls until it automatically reconnects
                updatePbState(PlaybackStateCompat.STATE_NONE)
            }

            override fun onConnectionFailed() {
                Log.e(Global.TAG, "Connection failed in RadioClient")
                // The Service has refused our connection
                updatePbState(PlaybackStateCompat.STATE_NONE)
            }
        }

    private fun connect() {
        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, RadioService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser?.connect()
    }

    fun onStart(activity: Activity) {
        this.activity = activity

        updatePbState(PlaybackStateCompat.STATE_CONNECTING)

        connect()

        activity.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    fun onStop() {
        MediaControllerCompat.getMediaController(activity)
            ?.unregisterCallback(controllerCallback)

        mediaBrowser?.disconnect()
    }

    private var controllerCallback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {}

            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                // To change the playback state inside the app when the user changes it
                // from the notification
                updatePbState(state.state)
            }
        }

    private fun buildTransportControls() {
        // Display the initial state
        updatePbState(controller.playbackState.state)

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback)
    }

    private fun updatePbState(state: Int) {
        when (state) {
            PlaybackStateCompat.STATE_PLAYING,
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_PAUSED,
            PlaybackStateCompat.STATE_CONNECTING ->
                _uiState.update { it.copy(
                    btnState = state
                )}
            else -> {}
        }
    }

    fun onPlayPause() {
        Log.d(Global.TAG, "Playing the radio0 ${controller.playbackState.state}")
        // Since this is a play/pause button
        // test the current state and choose the action accordingly=
        if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            Log.d(Global.TAG, "Playing the radio1")
            tc.pause()
            updatePbState(PlaybackStateCompat.STATE_STOPPED)
        }
        else if (controller.playbackState.state == PlaybackStateCompat.STATE_STOPPED
            || controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
            Log.d(Global.TAG, "Playing the radio2")
            tc.play()
            updatePbState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

}