package bassamalim.hidaya.viewmodel

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
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.repository.RadioClientRepo
import bassamalim.hidaya.services.RadioService
import bassamalim.hidaya.state.RadioClientState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RadioClientVM @Inject constructor(
    private val app: Application,
    private val repository: RadioClientRepo
): AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(RadioClientState())
    val uiState = _uiState.asStateFlow()

    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private var url: String? = null

    init {
        getLinkAndConnect()
    }

    fun onStart() {
        mediaBrowser?.connect()
        (app.applicationContext as Activity).volumeControlStream = AudioManager.STREAM_MUSIC
    }

    fun onStop() {
        MediaControllerCompat.getMediaController(
            app.applicationContext as Activity
        )?.unregisterCallback(controllerCallback)

        mediaBrowser?.disconnect()
    }

    private fun getLinkAndConnect() {
        url = repository.getLink()
        connect()
    }

    private fun connect() {
        val ctx = app.applicationContext
        mediaBrowser = MediaBrowserCompat(
            ctx,
            ComponentName(ctx, RadioService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser!!.connect()
    }

    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                if (url == null) {
                    Log.e(Global.TAG, "URL is null in RadioClient")
                    return
                }

                val ctx = app.applicationContext

                // Get the token for the MediaSession
                val token = mediaBrowser!!.sessionToken

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(ctx, token)

                val activity = ctx as Activity
                // Save the controller
                MediaControllerCompat.setMediaController(activity, mediaController)
                controller = MediaControllerCompat.getMediaController(activity)
                tc = controller.transportControls

                // just sending the static url to extract the dynamic url
                tc.playFromMediaId(url, null)

                // Finish building the UI
                buildTransportControls()

                _uiState.update { it.copy(
                    btnState = PlaybackStateCompat.STATE_STOPPED
                )}
            }

            override fun onConnectionSuspended() {
                Log.e(Global.TAG, "Connection suspended in RadioClient")
                // The Service has crashed.
                // Disable transport controls until it automatically reconnects
                _uiState.update { it.copy(
                    btnState = PlaybackStateCompat.STATE_NONE
                )}
            }

            override fun onConnectionFailed() {
                Log.e(Global.TAG, "Connection failed in RadioClient")
                // The Service has refused our connection
                _uiState.update { it.copy(
                    btnState = PlaybackStateCompat.STATE_NONE
                )}
            }
        }

    private var controllerCallback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {}

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
        when (state.state) {
            PlaybackStateCompat.STATE_PLAYING,
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_CONNECTING -> _uiState.update { it.copy(
                btnState = state.state
            )}
            else -> {}
        }
    }

    fun onPlayPause() {
        if (_uiState.value.btnState != PlaybackStateCompat.STATE_NONE) {
            // Since this is a play/pause button
            // test the current state and choose the action accordingly=
            if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                tc.pause()

                _uiState.update { it.copy(
                    btnState = PlaybackStateCompat.STATE_STOPPED
                )}
            }
            else {
                tc.play()

                _uiState.update { it.copy(
                    btnState = PlaybackStateCompat.STATE_PLAYING
                )}
            }
        }
    }

}