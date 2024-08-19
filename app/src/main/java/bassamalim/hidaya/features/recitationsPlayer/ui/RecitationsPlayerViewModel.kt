package bassamalim.hidaya.features.recitationsPlayer.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Reciter
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.recitationsPlayer.domain.RecitationsPlayerDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableApi::class)
@HiltViewModel
class RecitationsPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: RecitationsPlayerDomain,
    private val navigator: Navigator
): ViewModel() {

    private val action = savedStateHandle.get<String>("action") ?: ""
    private val mediaId = savedStateHandle.get<String>("media_id") ?: ""

    private lateinit var language: Language
    var reciterId = mediaId.substring(0, 3).toInt()
    var versionId = mediaId.substring(3, 5).toInt()
    var suraIdx = mediaId.substring(5).toInt()
    private lateinit var version: Reciter.RecitationVersion
    private lateinit var suraNames: List<String>
    var duration = 0L
    var progress = 0L

    private val _uiState = MutableStateFlow(RecitationsPlayerUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getRepeatMode(),
        domain.getShuffleMode()
    ) { state, repeatMode, shuffleMode ->
        state.copy(
            repeatMode = repeatMode,
            shuffleMode = shuffleMode
        )
    }.stateIn(
        initialValue = RecitationsPlayerUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    init {
        viewModelScope.launch {
            language = domain.getLanguage()
            suraNames = domain.getSuraNames(language)
        }

        _uiState.update { it.copy(
            reciterName = domain.getReciterName(id = reciterId, language = language)
        )}

        updateTrackState()
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            if (domain.isMediaBrowserInitialized()) return

            domain.initializeController(controllerCallback)

            // Finish building the UI
            buildTransportControls()

            if (action != "back" &&
                (domain.getState() == STATE_NONE ||
                        mediaId != domain.getMetadata()
                            .getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))
            ) {
                domain.sendPlayRequest(
                    mediaId = mediaId,
                    playType = action,
                    reciterName = _uiState.value.reciterName,
                    version = version
                )
            }
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

    fun onStart() {
        domain.connect(connectionCallbacks, onComplete)
    }

    fun onStop() {
        Log.i(Global.TAG, "in onStop of TelawatClient")

        domain.stopMediaBrowser(controllerCallback, onComplete)
    }

    private var onComplete = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            _uiState.update { it.copy(
                downloadState = domain.checkDownload()
            )}
        }
    }

    private fun updateTrackState() {
        version = domain.getVersion(reciterId, versionId).let {
            Reciter.RecitationVersion(
                versionId = versionId,
                server = it.url,
                rewaya = it.nameAr,
                suar = it.availableSuras
            )
        }

        _uiState.update { it.copy(
            suraName = suraNames[suraIdx],
            versionName = version.rewaya,
            reciterName = domain.getReciterName(id = reciterId, language = language),
            downloadState = domain.checkDownload()
        )}
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

        // Display the initial state
        updateMetadata(domain.getMetadata())
        updatePbState(domain.getPlaybackState())
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
            domain.disconnectMediaBrowser()
        }
    }

    private fun updateMetadata(metadata: MediaMetadataCompat) {
        suraIdx = metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
        duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

        domain.setPath("${"/Telawat/${reciterId}/${versionId}/"}$suraIdx.mp3")

        _uiState.update { it.copy(
            suraName = suraNames[suraIdx],
            duration = formatTime(duration),
            downloadState = domain.checkDownload()
        )}
    }

    private fun updatePbState(state: PlaybackStateCompat) {
        progress = state.position

        _uiState.update { it.copy(
            btnState = state.state,
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

    fun onBackPressed(activity: Activity) {
        if (activity.isTaskRoot) {
            navigator.navigate(
                Screen.TelawatSuar(
                    reciterId = reciterId.toString(),
                    versionId = versionId.toString()
                )
            ) {
                popUpTo(Screen.TelawatClient(action, mediaId).route) {
                    inclusive = true
                }
            }
        }
        else
            (activity as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
    }

    fun onPlayPauseClick() {
        if (_uiState.value.btnState != STATE_NONE) {
            if (domain.getState() == STATE_PLAYING) {
                domain.pause()
                _uiState.update { it.copy(
                    btnState = STATE_PAUSED
                )}
            }
            else {
                domain.resume()
                _uiState.update { it.copy(
                    btnState = STATE_PLAYING
                )}
            }
        }
    }

    fun onPreviousTrackClick() {
        domain.skipToPrevious()
    }

    fun onNextTrackClick() {
        domain.skipToNext()
    }

    fun onSliderChange(progress: Float) {
        this.progress = progress.toLong()
        _uiState.update { it.copy(
            progress = formatTime(progress.toLong())
        )}
    }

    fun onSliderChangeFinished() {
        domain.seekTo(progress)
    }

    fun onRepeatClick() {
        viewModelScope.launch {
            domain.setRepeatMode(
                if (_uiState.value.repeatMode == REPEAT_MODE_NONE) REPEAT_MODE_ONE
                else REPEAT_MODE_NONE
            )
        }
    }

    fun onShuffleClick() {
        viewModelScope.launch {
            domain.setShuffleMode(
                if (_uiState.value.shuffleMode == SHUFFLE_MODE_NONE) SHUFFLE_MODE_ALL
                else SHUFFLE_MODE_NONE
            )
        }
    }

    fun onDownloadClick() {
        if (_uiState.value.downloadState == DownloadState.NOT_DOWNLOADED) {
            _uiState.update { it.copy(
                downloadState = DownloadState.DOWNLOADING
            )}

            domain.downloadRecitation(
                version = version,
                suraIdx = suraIdx,
                suraName = suraNames[suraIdx]
            )
        }
        else {
            _uiState.update { it.copy(
                downloadState = DownloadState.NOT_DOWNLOADED
            )}

            domain.deleteRecitation()
        }
    }

}