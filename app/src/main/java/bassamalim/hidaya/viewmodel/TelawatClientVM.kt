package bassamalim.hidaya.viewmodel

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.*
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.enums.DownloadState
import bassamalim.hidaya.models.Reciter
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.repository.TelawatClientRepo
import bassamalim.hidaya.services.TelawatService
import bassamalim.hidaya.state.TelawatClientState
import bassamalim.hidaya.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TelawatClientVM @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val repo: TelawatClientRepo
): AndroidViewModel(app) {

    private val action = savedStateHandle.get<String>("action") ?: ""
    private val mediaId = savedStateHandle.get<String>("media_id") ?: ""

    private lateinit var activity: Activity
    var reciterId = mediaId.substring(0, 3).toInt()
    var versionId = mediaId.substring(3, 5).toInt()
    var suraIdx = mediaId.substring(5).toInt()
    private var mediaBrowser: MediaBrowserCompat? = null
    private lateinit var controller: MediaControllerCompat
    private lateinit var tc: MediaControllerCompat.TransportControls
    private lateinit var version: Reciter.RecitationVersion
    private val surahNames = repo.getSuraNames()
    private var prefix = ""
    var duration = 0L
    var progress = 0L

    private val _uiState = MutableStateFlow(TelawatClientState(
        reciterName = repo.getReciterName(reciterId)
    ))
    val uiState = _uiState.asStateFlow()

    init {
        updateTrackState()
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            if (mediaBrowser == null) return

            // Get the token for the MediaSession
            val token = mediaBrowser!!.sessionToken

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(app, token)

            // Save the controller
            MediaControllerCompat.setMediaController(
                activity,
                mediaController
            )

            // Finish building the UI
            buildTransportControls()

            if (action != "back" &&
                (controller.playbackState.state == STATE_NONE ||
                        mediaId != controller.metadata.getString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID
                )))
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

    fun onStart(activity: Activity) {
        this.activity = activity

        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, TelawatService::class.java),
            connectionCallbacks,
            null
        )
        if (MediaControllerCompat.getMediaController(activity) == null)
            mediaBrowser?.connect()

        activity.volumeControlStream = AudioManager.STREAM_MUSIC

        app.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun onStop() {
        Log.i(Global.TAG, "in onStop of TelawatClient")

        try {
            app.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        MediaControllerCompat.getMediaController(activity)
            ?.unregisterCallback(controllerCallback)

        mediaBrowser?.disconnect()
    }

    private var onComplete = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            _uiState.update { it.copy(
                downloadState = checkDownload()
            )}
        }
    }

    private fun checkDownload(): DownloadState {
        return if (File("${app.getExternalFilesDir(null)}$prefix").exists())
            DownloadState.Downloaded
        else
            DownloadState.NotDownloaded
    }

    private fun updateTrackState() {
        version = repo.getVersion(reciterId, versionId).let {
            Reciter.RecitationVersion(
                versionId, it.getUrl(), it.getRewaya(), it.getCount(), it.getSuras()
            )
        }

        _uiState.update { it.copy(
            suraName = surahNames[suraIdx],
            versionName = version.rewaya,
            reciterName = repo.getReciterName(reciterId),
            repeat = repo.getRepeatMode(),
            shuffle = repo.getShuffleMode(),
            downloadState = checkDownload()
        )}
    }

    private fun sendPlayRequest() {
        // Pass media data
        val bundle = Bundle()
        bundle.putString("play_type", action)
        bundle.putString("reciter_name", _uiState.value.reciterName)
        bundle.putSerializable("version", version)

        // Start Playback
        tc.playFromMediaId(mediaId, bundle)
    }

    private fun enableControls() {
        _uiState.update { it.copy(
            btnState = STATE_PLAYING,
            controlsEnabled = true
        )}
    }

    private fun disableControls() {
        _uiState.update { it.copy(
            controlsEnabled = false
        )}
    }

    private fun buildTransportControls() {
        enableControls()

        controller = MediaControllerCompat.getMediaController(activity)
        tc = controller.transportControls

        // Display the initial state
        updateMetadata(controller.metadata)
        updatePbState(controller.playbackState)

        // Register a Callback to stay in sync
        controller.registerCallback(controllerCallback)
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {
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
            mediaBrowser?.disconnect()
        }
    }

    private fun updateMetadata(metadata: MediaMetadataCompat) {
        suraIdx = metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
        duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

        prefix = "${"/Telawat/${reciterId}/${versionId}/"}$suraIdx.mp3"

        _uiState.update { it.copy(
            suraName = surahNames[suraIdx],
            duration = formatTime(duration),
            downloadState = checkDownload()
        )}
    }

    private fun updatePbState(state: PlaybackStateCompat) {
        progress = state.position

        _uiState.update { it.copy(
            progress = formatTime(progress),
            secondaryProgress = state.bufferedPosition
        )}
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

    private fun download() {
        _uiState.update { it.copy(
            downloadState = DownloadState.Downloading
        )}

        val server = version.server
        val link = String.format(Locale.US, "%s/%03d.mp3", server, suraIdx + 1)
        val uri = Uri.parse(link)

        val request = DownloadManager.Request(uri)
        request.setTitle(_uiState.value.suraName)
        FileUtils.createDir(app, prefix)
        request.setDestinationInExternalFilesDir(app, prefix, "${suraIdx}.mp3")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        (app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }

    fun onBackPressed(navController: NavController) {
        if (activity.isTaskRoot) {
            navController.navigate(
                Screen.TelawatSuar(
                    reciterId.toString(),
                    versionId.toString()
                ).route
            ) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
        else (activity as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
    }

    fun onPlayPauseClk() {
        if (_uiState.value.btnState != STATE_NONE) {
            if (controller.playbackState.state == STATE_PLAYING) {
                tc.pause()
                _uiState.update { it.copy(
                    btnState = STATE_PAUSED
                )}
            }
            else {
                tc.play()
                _uiState.update { it.copy(
                    btnState = STATE_PLAYING
                )}
            }
        }
    }

    fun onFastForwardClk() {
        tc.fastForward()
    }

    fun onRewindClk() {
        tc.rewind()
    }

    fun onPrevClk() {
        tc.skipToPrevious()
    }

    fun onNextClk() {
        tc.skipToNext()
    }

    fun onSliderChange(progress: Float) {
        this.progress = progress.toLong()
        _uiState.update { it.copy(
            progress = formatTime(progress.toLong())
        )}
    }

    fun onSliderChangeFinished() {
        tc.seekTo(progress)
    }

    fun onRepeatClk() {
        val mode =
            if (_uiState.value.repeat == REPEAT_MODE_NONE) REPEAT_MODE_ONE
            else REPEAT_MODE_NONE

        _uiState.update { it.copy(
            repeat = mode
        )}

        tc.setRepeatMode(mode)

        repo.setRepeatMode(mode)
    }

    fun onShuffleClk() {
        val mode =
            if (_uiState.value.shuffle == SHUFFLE_MODE_NONE) SHUFFLE_MODE_ALL
            else SHUFFLE_MODE_NONE

        _uiState.update { it.copy(
            shuffle = mode
        )}

        tc.setShuffleMode(mode)

        repo.setShuffleMode(mode)
    }

    fun onDownloadClk() {
        download()
    }

    fun onDelete() {
        _uiState.update { it.copy(
            downloadState = DownloadState.NotDownloaded
        )}
    }

}