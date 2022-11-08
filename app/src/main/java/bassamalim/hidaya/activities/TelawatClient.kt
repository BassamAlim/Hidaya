package bassamalim.hidaya.activities

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.models.Reciter.RecitationVersion
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.TelawatService
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatClient : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var pref: SharedPreferences
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private lateinit var action: String
    private lateinit var mediaId: String
    private var reciterId = 0
    private var versionId = 0
    private var surahIndex = 0
    private lateinit var version: RecitationVersion
    private lateinit var surahNames: List<String>
    private val suraName = mutableStateOf("")
    private val versionName = mutableStateOf("")
    private val reciterName = mutableStateOf("")
    private val repeat = mutableStateOf(PlaybackStateCompat.REPEAT_MODE_NONE)
    private val shuffle = mutableStateOf(PlaybackStateCompat.SHUFFLE_MODE_NONE)
    private val duration = mutableStateOf(0L)
    private val btnState = mutableStateOf(PlaybackStateCompat.STATE_NONE)
    private val progress = mutableStateOf(0L)
    private val secondaryProgress = mutableStateOf(0)
    private val controlsEnabled = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        db = DBUtils.getDB(this)
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        data

        retrieveStates()

        suraName.value = surahNames[surahIndex]
        versionName.value = version.getRewaya()

        setContent {
            AppTheme {
                UI()
            }
        }

        mediaBrowser = MediaBrowserCompat(
            this, ComponentName(this, TelawatService::class.java),
            connectionCallbacks, null
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(Global.TAG, "In OnNewIntent")

        data

        sendPlayRequest() // Maybe this causes the problem of restarting when device unlocked
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        Log.i(Global.TAG, "in onStop of TelawatClient")
        super.onStop()
        if (MediaControllerCompat.getMediaController(this@TelawatClient) != null) {
            MediaControllerCompat.getMediaController(this@TelawatClient)
                .unregisterCallback(controllerCallback)
        }
        mediaBrowser.disconnect()
    }

    private val connectionCallbacks: MediaBrowserCompat.ConnectionCallback = object :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Get the token for the MediaSession
            val token = mediaBrowser.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(this@TelawatClient, token)

            // Save the controller
            MediaControllerCompat.setMediaController(this@TelawatClient, mediaController)

            // Finish building the UI
            buildTransportControls()

            data

            if (action != "back" &&
                (controller.playbackState.state == PlaybackStateCompat.STATE_NONE ||
                        mediaId != controller.metadata.getString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID))
            )
                sendPlayRequest()
        }

        override fun onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in TelawatClient")
            // The Service has crashed.
            // Disable transport controls until it automatically reconnects
            disableControls()
        }

        override fun onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in TelawatClient")
            // The Service has refused our connection
            disableControls()
        }
    }
    private val data: Unit
        get() {
            action = intent.action!!
            mediaId = intent.getStringExtra("media_id")!!

            reciterId = mediaId.substring(0, 3).toInt()
            versionId = mediaId.substring(3, 5).toInt()
            surahIndex = mediaId.substring(5).toInt()

            reciterName.value = db.telawatRecitersDao().getName(reciterId)

            val telawa = db.telawatVersionsDao().getVersion(reciterId, versionId)
            version = RecitationVersion(
                versionId, telawa.getUrl(), telawa.getRewaya(), telawa.getCount(), telawa.getSuras()
            )

            surahNames = db.suarDao().getNames()
        }

    private fun retrieveStates() {
        repeat.value = pref.getInt("telawat_repeat_mode", 0)
        shuffle.value = pref.getInt("telawat_shuffle_mode", 0)
    }

    private fun sendPlayRequest() {
        // Pass media data
        val bundle = Bundle()
        bundle.putString("play_type", action)
        bundle.putString("reciter_name", reciterName.value)
        bundle.putSerializable("version", version)

        // Start Playback
        tc.playFromMediaId(mediaId, bundle)
    }

    private fun enableControls() {
        controlsEnabled.value = true
        btnState.value = PlaybackStateCompat.STATE_PLAYING
    }

    private fun disableControls() {
        controlsEnabled.value = false
    }

    private fun buildTransportControls() {
        enableControls()

        controller = MediaControllerCompat.getMediaController(this@TelawatClient)
        tc = controller.transportControls

        // Display the initial state
        updateMetadata(controller.metadata)
        updatePbState(controller.playbackState)

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback)
    }

    private var controllerCallback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            // To change the metadata inside the app when the user changes it from the notification
            updateMetadata(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            // To change the playback state inside the app when the user changes it
            // from the notification
            updatePbState(state)
        }

        override fun onSessionDestroyed() {
            mediaBrowser.disconnect()
        }
    }

    private fun updateMetadata(metadata: MediaMetadataCompat) {
        surahIndex = metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
        suraName.value = surahNames[surahIndex]

        duration.value = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
    }

    private fun updatePbState(state: PlaybackStateCompat) {
        progress.value = state.position
        secondaryProgress.value = state.bufferedPosition.toInt()
    }

    private fun formatTime(timeInMillis: Long): String {
        val hours = timeInMillis / (60 * 60 * 1000) % 24
        val minutes = timeInMillis / (60 * 1000) % 60
        val seconds = timeInMillis / 1000 % 60
        var hms = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        if (hms.startsWith("0")) {
            hms = hms.substring(1)
            if (hms.startsWith("0")) hms = hms.substring(2)
        }
        return hms
    }

    override fun onBackPressed() {
        onBack()
    }

    private fun onBack() {
        val intent = Intent(this, TelawatSuarActivity::class.java)
        intent.putExtra("reciter_id", reciterId)
        intent.putExtra("reciter_name", reciterName.value)
        intent.putExtra("version_id", versionId)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        Log.i(Global.TAG, "in onDestroy of TelawatClient")
        super.onDestroy()
        mediaBrowser.disconnect()
    }

    @Composable
    private fun UI() {
        MyScaffold(
            title = stringResource(id = R.string.recitations),
            bottomBar = {
                BottomAppBar(
                    backgroundColor = AppTheme.colors.primary
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MyIconBtn(
                            iconId = R.drawable.ic_repeat,
                            description = stringResource(R.string.repeat_description),
                            tint =
                                if (repeat.value == PlaybackStateCompat.REPEAT_MODE_ONE)
                                    AppTheme.colors.secondary
                                else AppTheme.colors.onPrimary
                        ) {
                            if (repeat.value == PlaybackStateCompat.REPEAT_MODE_NONE) {
                                repeat.value = PlaybackStateCompat.REPEAT_MODE_ONE
                                tc.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)

                                pref.edit()
                                    .putInt("telawat_repeat_mode", repeat.value)
                                    .apply()
                            }
                            else if (repeat.value == PlaybackStateCompat.REPEAT_MODE_ONE) {
                                repeat.value = PlaybackStateCompat.REPEAT_MODE_NONE
                                tc.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)

                                pref.edit()
                                    .putInt("telawat_repeat_mode", repeat.value)
                                    .apply()
                            }
                        }

                        MyIconBtn(
                            iconId = R.drawable.ic_download,
                            description = stringResource(R.string.download_description),
                            modifier = Modifier.alpha(0F)
                        ) {

                        }

                        MyIconBtn(
                            iconId = R.drawable.ic_shuffle,
                            description = stringResource(R.string.shuffle_description),
                            tint =
                                if (shuffle.value == PlaybackStateCompat.SHUFFLE_MODE_ALL)
                                    AppTheme.colors.secondary
                                else AppTheme.colors.onPrimary
                        ) {
                            if (shuffle.value == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
                                shuffle.value = PlaybackStateCompat.SHUFFLE_MODE_ALL
                                tc.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)

                                pref.edit()
                                    .putInt("telawat_shuffle_mode", shuffle.value)
                                    .apply()
                            }
                            else if (shuffle.value == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                                shuffle.value = PlaybackStateCompat.SHUFFLE_MODE_NONE
                                tc.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)

                                pref.edit()
                                    .putInt("telawat_shuffle_mode", shuffle.value)
                                    .apply()
                            }
                        }
                    }
                }
            }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .border(
                            width = 2.dp,
                            shape = RoundedCornerShape(10),
                            color = AppTheme.colors.accent
                        )
                ) {
                    Column(
                        Modifier
                            .padding(vertical = 25.dp, horizontal = 75.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        MyText(
                            text = suraName.value,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        MyText(
                            text = versionName.value,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        MyText(
                            text = reciterName.value,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.weakPrimary),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MyText(
                        text = formatTime(progress.value),
                        modifier = Modifier.padding(10.dp)
                    )

                    MySlider(
                        value = progress.value.toFloat(),
                        valueRange = 0F..duration.value.toFloat(),
                        modifier = Modifier.weight(1F),
                        enabled = controlsEnabled.value,
                        onValueChangeFinished = { tc.seekTo(progress.value) },
                        onValueChange = { newValue -> progress.value = newValue.toLong() }
                    )

                    MyText(
                        text = formatTime(duration.value),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.weakPrimary),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MyImageButton(
                        imageResId = R.drawable.ic_player_previous,
                        description = stringResource(R.string.previous_day_button_description),
                        enabled = controlsEnabled.value
                    ) {
                        tc.skipToPrevious()
                    }

                    MyImageButton(
                        imageResId = R.drawable.ic_backward,
                        description = stringResource(R.string.rewind_btn_description),
                        enabled = controlsEnabled.value
                    ) {
                        tc.rewind()
                    }

                    MyPlayerBtn(
                        state = btnState,
                        enabled = controlsEnabled.value,
                    ) {
                        if (btnState.value != PlaybackStateCompat.STATE_NONE) {
                            // Since this is a play/pause button
                            // test the current state and choose the action accordingly=
                            if (controller.playbackState.state ==
                                PlaybackStateCompat.STATE_PLAYING) {
                                tc.pause()
                                btnState.value = PlaybackStateCompat.STATE_STOPPED
                            }
                            else {
                                tc.play()
                                btnState.value = PlaybackStateCompat.STATE_PLAYING
                            }
                        }
                    }

                    MyImageButton(
                        imageResId = R.drawable.ic_forward,
                        description = stringResource(R.string.fast_forward_btn_description),
                        enabled = controlsEnabled.value
                    ) {
                        tc.fastForward()
                    }

                    MyImageButton(
                        imageResId = R.drawable.ic_player_next,
                        description = stringResource(R.string.next_track_btn_description),
                        enabled = controlsEnabled.value
                    ) {
                        tc.skipToNext()
                    }
                }
            }
        }
    }

}