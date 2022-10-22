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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.RadioService
import bassamalim.hidaya.ui.components.MyPlayerBtn
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

@RequiresApi(Build.VERSION_CODES.O)
class RadioClient : ComponentActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private lateinit var url: String
    private val btnState = mutableStateOf(PlaybackStateCompat.STATE_NONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        setContent {
            AppTheme {
                UI()
            }
        }

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

            btnState.value = PlaybackStateCompat.STATE_STOPPED
        }

        override fun onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in RadioClient")
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
            btnState.value = PlaybackStateCompat.STATE_NONE
        }

        override fun onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in RadioClient")
            // The Service has refused our connection
            btnState.value = PlaybackStateCompat.STATE_NONE
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
        when (state.state) {
            PlaybackStateCompat.STATE_PLAYING,
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_CONNECTING -> btnState.value = state.state
            else -> {}
        }
    }

    @Composable
    private fun UI() {
        MyScaffold(stringResource(id = R.string.quran_radio)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    text = stringResource(R.string.holy_quran_radio),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 50.dp)
                )

                MyPlayerBtn(state = btnState, padding = 10.dp) {
                    if (btnState.value != PlaybackStateCompat.STATE_NONE) {
                        // Since this is a play/pause button
                        // test the current state and choose the action accordingly=
                        if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                            tc.pause()
                            btnState.value = PlaybackStateCompat.STATE_STOPPED
                        }
                        else {
                            tc.play()
                            btnState.value = PlaybackStateCompat.STATE_PLAYING
                        }
                    }
                }
            }
        }
    }
}