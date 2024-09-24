package bassamalim.hidaya.features.radio.ui

import android.app.Activity
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.radio.domain.RadioDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RadioClientViewModel @Inject constructor(
    private val domain: RadioDomain
): ViewModel() {

    private val _uiState = MutableStateFlow(RadioClientUiState())
    val uiState = _uiState.asStateFlow()

    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                domain.initializeController()

                // just sending the static url to extract the dynamic url
                val url = domain.getUrl()
                domain.play(url)

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

    fun onStart(activity: Activity) {
        domain.setActivity(activity)

        updatePbState(PlaybackStateCompat.STATE_CONNECTING)

        domain.connect(connectionCallbacks)
    }

    fun onStop() {
        domain.disconnect(controllerCallback)
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
        updatePbState(domain.getState())

        // Register a Callback to stay in sync
        domain.registerCallback(controllerCallback)
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

    fun onPlayPauseClick() {
        when (domain.getState()) {
            PlaybackStateCompat.STATE_PLAYING -> {
                domain.pause()
                updatePbState(PlaybackStateCompat.STATE_STOPPED)
            }
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_PAUSED -> {
                domain.resume()
                updatePbState(PlaybackStateCompat.STATE_PLAYING)
            }
            else -> {}
        }
    }

}