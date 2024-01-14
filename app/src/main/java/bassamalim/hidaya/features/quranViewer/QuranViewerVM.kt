package bassamalim.hidaya.features.quranViewer

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.QuranViewTypes
import bassamalim.hidaya.core.models.Aya
import bassamalim.hidaya.core.other.Global
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

@kotlin.OptIn(ExperimentalPagerApi::class)
@HiltViewModel
class QuranViewerVM @Inject constructor(
    private val app: Application,
    private val repo: QuranViewerRepo,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(app) {

    private val targetType = QuranTarget.valueOf(
        savedStateHandle.get<String>("target_type") ?: QuranTarget.SURA.name
    )
    private var targetValue = savedStateHandle.get<Int>("target_value") ?: 0

    private lateinit var activity: Activity
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var pagerState: PagerState
    private lateinit var scrollState: ScrollState
    val language = repo.getLanguage()
    val numeralsLanguage = repo.getNumeralsLanguage()
    val theme = repo.getTheme()
    var initialPageNum = when (targetType) {
        QuranTarget.PAGE -> targetValue
        QuranTarget.SURA -> repo.getSuraPageNum(targetValue)
        QuranTarget.AYA -> repo.getAyaPageNum(targetValue)
    }
        private set
    private val suraNames =
        if (language == Language.ENGLISH) repo.getSuraNamesEn()
        else repo.getSuraNames()
    private val ayat = repo.getAyat()
    private val handler = Handler(Looper.getMainLooper())
    private var suraNum = 0
    private var bookmarkedPage = repo.getBookmarkedPage()
    val pref = repo.sp
    private var lastRecordedPage = 0
    private var lastClickT = 0L
    private var lastClickedId = -1
    var scrollTo = -1F
        private set
    private var mediaBrowser: MediaBrowserCompat? = null
    private var controller: MediaControllerCompat? = null
    private var tc: MediaControllerCompat.TransportControls? = null
    private val ayaPositions = mutableMapOf<Int, Float>()

    private val _uiState = MutableStateFlow(QuranViewerState(
        pageNum = initialPageNum,
        viewType =
            if (language == Language.ENGLISH) QuranViewTypes.List
            else repo.getViewType(),
        textSize = repo.getTextSize(),
        isBookmarked = bookmarkedPage == initialPageNum,
        pageAyat = buildPage(initialPageNum),
        tutorialDialogShown = repo.getShowTutorial()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        if (targetType == QuranTarget.AYA) {
            _uiState.update { it.copy(
                selectedAya = _uiState.value.pageAyat.first { it.id == targetValue }
            )}
        }
    }

    fun onStart(activity: Activity, pagerState: PagerState, coroutineScope: CoroutineScope) {
        this.activity = activity
        this.pagerState = pagerState
        this.coroutineScope = coroutineScope
    }

    fun onStop() {
        MediaControllerCompat.getMediaController(activity)
            ?.unregisterCallback(controllerCallback)

        mediaBrowser?.disconnect()
        mediaBrowser = null

        handler.removeCallbacks(runnable)
    }

    fun onPageChange(currentPageIdx: Int, pageIdx: Int) {
        if (currentPageIdx == pageIdx) {
            updatePageState(pageIdx + 1) // page number = page index + 1

            if (pageIdx != lastRecordedPage) {
                handler.removeCallbacks(runnable)
                checkPage()
            }
        }
    }

    fun onBookmarkClick() {
        if (_uiState.value.isBookmarked) {
            bookmarkedPage = -1

            repo.setBookmarkedPage(
                pageNum = _uiState.value.pageNum,
                suraNum = -1
            )
        }
        else {
            bookmarkedPage = _uiState.value.pageNum

            repo.setBookmarkedPage(
                pageNum = _uiState.value.pageNum,
                suraNum = suraNum
            )
        }

        _uiState.update { it.copy(
            isBookmarked = !_uiState.value.isBookmarked
        )}
    }

    fun onPlayPauseClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(
                activity,
                activity.getString(R.string.feature_not_supported),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (mediaBrowser == null) {
            updateButton(PlaybackStateCompat.STATE_BUFFERING)
            setupPlayer()
        }
        else {
            when (controller!!.playbackState.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    updateButton(PlaybackStateCompat.STATE_PAUSED)
                    tc!!.pause()
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    updateButton(PlaybackStateCompat.STATE_BUFFERING)

                    if (_uiState.value.selectedAya == null) {
                        tc!!.play()

                        _uiState.update { it.copy(
                            selectedAya = null
                        )}
                    }
                    else
                        requestPlay(_uiState.value.selectedAya!!.id)
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    updateButton(PlaybackStateCompat.STATE_BUFFERING)

                    if (_uiState.value.selectedAya == null) {
                        _uiState.update { it.copy(
                            selectedAya = _uiState.value.pageAyat[0]
                        )}
                    }

                    requestPlay(_uiState.value.selectedAya!!.id)
                }
                else -> {}
            }
        }
    }

    fun onPreviousAyaClk() {
        tc?.skipToPrevious()
    }

    fun onNextAyaClk() {
        tc?.skipToNext()
    }

    fun onSettingsClick() {
        _uiState.update { it.copy(
            settingsDialogShown = true
        )}
    }

    fun onAyaScreenClick(ayaId: Int, offset: Int) {
        val maxDuration = 1200

        var aya: Aya? = null
        when (_uiState.value.viewType) {
            QuranViewTypes.Page -> {
                val startIdx = _uiState.value.pageAyat.indexOfFirst { it.id == ayaId }
                for (idx in startIdx until _uiState.value.pageAyat.size) {
                    val a = _uiState.value.pageAyat[idx]
                    if (offset < a.end)
                        aya = a
                }
            }
            QuranViewTypes.List -> aya = _uiState.value.pageAyat.find { it.id == ayaId }
        }

        // double click
        if (aya?.id == lastClickedId
            && System.currentTimeMillis() < lastClickT + maxDuration) {
            _uiState.update { it.copy(
                selectedAya = null
            )}

            _uiState.update { it.copy(
                infoDialogShown = true,
                infoDialogText = aya.tafseer
            )}
        }
        else {  // single click
            if (_uiState.value.selectedAya == aya) {
                _uiState.update { it.copy(
                    selectedAya = null
                )}
            }
            else {
                _uiState.update { it.copy(
                    selectedAya = aya
                )}
            }
        }

        lastClickedId = aya?.id ?: -1
        lastClickT = System.currentTimeMillis()
    }

    fun onSuraHeaderGloballyPositioned(
        aya: Aya,
        isCurrentPage: Boolean,
        layoutCoordinates: LayoutCoordinates
    ) {
        if (
            isCurrentPage
            && scrollTo == -1F
            && targetType == QuranTarget.SURA
            && aya.suraNum == targetValue+1
            ) {
            scrollTo = layoutCoordinates.positionInParent().y - 13
        }
    }

    fun onAyaGloballyPositioned(
        aya: Aya,
        isCurrentPage: Boolean,
        layoutCoordinates: LayoutCoordinates
    ) {
        val screenHeight = app.resources.displayMetrics.heightPixels
        if (isCurrentPage && _uiState.value.viewType == QuranViewTypes.List)
            ayaPositions[aya.id] = layoutCoordinates.positionInParent().y - screenHeight / 3f
    }

    fun onScrolled() {
        scrollTo = 0F
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            tutorialDialogShown = false
        )}

        if (doNotShowAgain) repo.setDoNotShowTutorial()
    }

    fun onInfoDialogDismiss() {
        _uiState.update { it.copy(
            infoDialogShown = false
        )}
    }

    fun onSettingsDialogDismiss() {
        _uiState.update { it.copy(
            settingsDialogShown = false,
            viewType = repo.getViewType(),
            textSize = repo.getTextSize()
        )}
    }

    fun setScrollState(scrollState: ScrollState) {
        this.scrollState = scrollState
    }

    fun buildPage(pageNumber: Int): ArrayList<Aya> {
        val pageAyat = ArrayList<Aya>()

        // get page start
        var counter = ayat.indexOfFirst { aya -> aya.page == pageNumber }
        do {
            val aya = ayat[counter]
            val suraNum = aya.suraNum // starts from 1
            val ayaNum = aya.ayaNum

            pageAyat.add(
                Aya(
                    id = aya.id,
                    juz = aya.juz,
                    suraNum = suraNum,
                    suraName = suraNames[suraNum - 1],
                    ayaNum = ayaNum,
                    text = "${aya.ayaText} ",
                    translation = aya.translationEn,
                    tafseer = aya.tafseer
                )
            )

            counter++
        } while (counter != Global.QURAN_AYAT && ayat[counter].page == pageNumber)

        return pageAyat
    }

    private fun updatePageState(pageNumber: Int) {
        suraNum = ayat.first { aya -> aya.page == pageNumber }.suraNum - 1
        _uiState.update { it.copy(
            pageNum = pageNumber,
            suraName = suraNames[suraNum],
            juzNum = ayat.first { aya -> aya.page == pageNumber }.juz,
            pageAyat = buildPage(pageNumber),
            isBookmarked = bookmarkedPage == pageNumber
        )}
    }

    private fun checkPage() {
        lastRecordedPage = _uiState.value.pageNum
        handler.postDelayed(runnable, 40000)
    }

    private val runnable = Runnable {
        if (_uiState.value.pageNum == lastRecordedPage)
            updateRecords()
    }

    private fun updateRecords() {
        val old = repo.getPagesRecord()
        val new = old + 1

        repo.setPagesRecord(new)

        if (_uiState.value.pageNum == repo.getWerdPage())
            repo.setWerdDone()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    private fun setupPlayer() {
        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, AyaPlayerService::class.java),
            connectionCallbacks,
            null
        )
        mediaBrowser?.connect()

        activity.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.i(Global.TAG, "In onServiceConnected")

            if (mediaBrowser == null) return

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(app, mediaBrowser!!.sessionToken)

            // Save the controller
            MediaControllerCompat.setMediaController(activity, mediaController)

            if (_uiState.value.selectedAya == null) {
                _uiState.update { it.copy(
                    selectedAya = _uiState.value.pageAyat[0]
                )}
            }

            // Finish building the UI
            buildTransportControls()

            requestPlay(_uiState.value.selectedAya!!.id)
        }

        override fun onConnectionSuspended() {
            Log.e(Global.TAG, "Connection suspended in TelawatClient")
            // The Service has crashed.
        }

        override fun onConnectionFailed() {
            Log.e(Global.TAG, "Connection failed in TelawatClient")
            // The Service has refused our connection
        }
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _uiState.update { it.copy(
                trackedAyaId = metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
            )}
            if (_uiState.value.viewType == QuranViewTypes.List) {
                coroutineScope.launch {
                    Log.d(Global.TAG, "Scrolling to ${_uiState.value.trackedAyaId} at ${ayaPositions[_uiState.value.trackedAyaId]}")
                    if (ayaPositions[_uiState.value.trackedAyaId] != null)
                        scrollState.animateScrollTo(ayaPositions[_uiState.value.trackedAyaId]!!.toInt())
                }
            }

            val pageNum = metadata.getLong("page_num").toInt()
            if (pageNum != _uiState.value.pageNum) {
                viewModelScope.launch {
                    pagerState.scrollToPage(pageNum - 1)
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            // To change the playback state inside the app when the user changes it
            // from the notification
            updateButton(state.state)

            if (state.state == PlaybackStateCompat.STATE_STOPPED) {
                _uiState.update { it.copy(
                    trackedAyaId = -1
                )}
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowser?.disconnect()
        }
    }

    private fun buildTransportControls() {
        controller = MediaControllerCompat.getMediaController(activity)
        tc = controller!!.transportControls

        // Register a Callback to stay in sync
        controller?.registerCallback(controllerCallback)
    }

    private fun requestPlay(ayaId: Int) {
        Executors.newSingleThreadExecutor().execute {
            tc?.playFromMediaId(ayaId.toString(), Bundle())

            _uiState.update { it.copy(
                selectedAya = null
            )}
        }
    }

    private fun updateButton(state: Int) {
        when (state) {
            PlaybackStateCompat.STATE_NONE,
            PlaybackStateCompat.STATE_PAUSED,
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_PLAYING,
            PlaybackStateCompat.STATE_BUFFERING -> {
                _uiState.update { it.copy(
                    playerState = state
                )}
            }
            else -> {}
        }
    }

}