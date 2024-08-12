package bassamalim.hidaya.features.quranReader.ui

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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.models.Verse
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.features.quranReader.ayaPlayer.AyaPlayerService
import bassamalim.hidaya.features.quranReader.domain.QuranReaderDomain
import bassamalim.hidaya.features.quranReader.domain.QuranTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

@kotlin.OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class QuranReaderViewModel @Inject constructor(
    private val app: Application,
    private val domain: QuranReaderDomain,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(app) {

    private val targetType = QuranTarget.valueOf(
        savedStateHandle.get<String>("target_type") ?: QuranTarget.SURA.name
    )
    private var targetValue = savedStateHandle.get<Int>("target_value") ?: 0

    lateinit var language: Language
    lateinit var numeralsLanguage: Language
    lateinit var theme: Theme
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var pagerState: PagerState
    private lateinit var scrollState: ScrollState
    var initialPageNum = when (targetType) {
        QuranTarget.PAGE -> targetValue
        QuranTarget.SURA -> domain.getSuraPageNum(targetValue)
        QuranTarget.AYA -> domain.getAyaPageNum(targetValue)
    }
        private set
    private lateinit var suraNames: List<String>
    private val ayat = repo.getAyat()
    private val handler = Handler(Looper.getMainLooper())
    private var suraNum = 0
    private var bookmarkedPage = repo.getBookmarkedPage()
    private var lastRecordedPage = 0
    private var lastClickT = 0L
    private var lastClickedId = -1
    var scrollTo = -1F
        private set
    private var mediaBrowser: MediaBrowserCompat? = null
    private var controller: MediaControllerCompat? = null
    private var tc: MediaControllerCompat.TransportControls? = null
    private val ayaPositions = mutableMapOf<Int, Float>()

    private val _uiState = MutableStateFlow(QuranReaderUiState(
        pageNum = initialPageNum,
        viewType =
            if (language == Language.ENGLISH) QuranViewType.LIST
            else repo.getViewType(),
        textSize = repo.getTextSize(),
        isBookmarked = bookmarkedPage == initialPageNum,
        pageVerses = buildPage(initialPageNum),
        tutorialDialogShown = repo.getShowTutorial()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()
            theme = domain.getTheme()

            suraNames = domain.getSuraNames(language)
        }


        if (targetType == QuranTarget.AYA) {
            _uiState.update { it.copy(
                selectedVerse = it.pageVerses.first { it.id == targetValue }
            )}
        }
    }

    fun onStart(pagerState: PagerState, coroutineScope: CoroutineScope) {
        this.pagerState = pagerState
        this.coroutineScope = coroutineScope
    }

    fun onStop() {
        MediaControllerCompat.getMediaController(app.applicationContext as Activity)
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

            viewModelScope.launch {
                domain.setBookmarkedPage(
                    pageNum = _uiState.value.pageNum,
                    suraNum = -1
                )
            }
        }
        else {
            bookmarkedPage = _uiState.value.pageNum

            viewModelScope.launch {
                domain.setBookmarkedPage(
                    pageNum = _uiState.value.pageNum,
                    suraNum = suraNum
                )
            }
        }

        _uiState.update { it.copy(
            isBookmarked = !it.isBookmarked
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

        if (mediaBrowser == null || controller == null) {
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

                    if (_uiState.value.selectedVerse == null) {
                        tc!!.play()

                        _uiState.update { it.copy(
                            selectedVerse = null
                        )}
                    }
                    else
                        requestPlay(_uiState.value.selectedVerse!!.id)
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    updateButton(PlaybackStateCompat.STATE_BUFFERING)

                    if (_uiState.value.selectedVerse == null) {
                        _uiState.update { it.copy(
                            selectedVerse = it.pageVerses[0]
                        )}
                    }

                    requestPlay(_uiState.value.selectedVerse!!.id)
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

        var verse: Verse? = null
        when (_uiState.value.viewType) {
            QuranViewType.PAGE -> {
                val startIdx = _uiState.value.pageVerses.indexOfFirst { it.id == ayaId }
                for (idx in startIdx until _uiState.value.pageVerses.size) {
                    val a = _uiState.value.pageVerses[idx]
                    if (offset < a.end) {
                        verse = a
                        break
                    }
                }
            }
            QuranViewType.LIST -> verse = _uiState.value.pageVerses.find { it.id == ayaId }
        }

        // double click
        if (verse?.id == lastClickedId
            && System.currentTimeMillis() < lastClickT + maxDuration) {
            _uiState.update { it.copy(
                selectedVerse = null
            )}

            _uiState.update { it.copy(
                infoDialogShown = true,
                infoDialogText = verse.tafseer
            )}
        }
        else {  // single click
            if (_uiState.value.selectedVerse == verse) {
                _uiState.update { it.copy(
                    selectedVerse = null
                )}
            }
            else {
                _uiState.update { it.copy(
                    selectedVerse = verse
                )}
            }
        }

        lastClickedId = verse?.id ?: -1
        lastClickT = System.currentTimeMillis()
    }

    fun onSuraHeaderGloballyPositioned(
        verse: Verse,
        isCurrentPage: Boolean,
        layoutCoordinates: LayoutCoordinates
    ) {
        if (
            isCurrentPage
            && scrollTo == -1F
            && targetType == QuranTarget.SURA
            && verse.suraNum == targetValue+1
            ) {
            scrollTo = layoutCoordinates.positionInParent().y - 13
        }
    }

    fun onAyaGloballyPositioned(
        verse: Verse,
        isCurrentPage: Boolean,
        layoutCoordinates: LayoutCoordinates
    ) {
        val screenHeight = app.resources.displayMetrics.heightPixels
        if (isCurrentPage && _uiState.value.viewType == QuranViewType.LIST)
            ayaPositions[verse.id] = layoutCoordinates.positionInParent().y - screenHeight / 3f
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

    fun buildPage(pageNumber: Int): ArrayList<Verse> {
        val pageAyat = ArrayList<Verse>()

        // get page start
        var counter = ayat.indexOfFirst { aya -> aya.page == pageNumber }
        do {
            val aya = ayat[counter]
            val suraNum = aya.suraNum // starts from 1
            val ayaNum = aya.ayaNum

            pageAyat.add(
                Verse(
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
            pageVerses = buildPage(pageNumber),
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

            val mediaController: MediaControllerCompat?
            try {
                // Create a MediaControllerCompat
                mediaController = MediaControllerCompat(app, mediaBrowser!!.sessionToken)
            } catch (e: IllegalStateException) {
                Log.e(Global.TAG, "Error in TelawatClient: ${e.message}")
                return
            }

            // Save the controller
            MediaControllerCompat.setMediaController(activity, mediaController)

            if (_uiState.value.selectedVerse == null) {
                _uiState.update { it.copy(
                    selectedVerse = it.pageVerses[0]
                )}
            }

            // Finish building the UI
            buildTransportControls()

            requestPlay(_uiState.value.selectedVerse!!.id)
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
                trackedVerseId = metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
            )}
            if (_uiState.value.viewType == QuranViewType.LIST) {
                coroutineScope.launch {
                    Log.d(Global.TAG, "Scrolling to ${_uiState.value.trackedVerseId} at ${ayaPositions[_uiState.value.trackedVerseId]}")
                    if (ayaPositions[_uiState.value.trackedVerseId] != null)
                        scrollState.animateScrollTo(ayaPositions[_uiState.value.trackedVerseId]!!.toInt())
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
                    trackedVerseId = -1
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
                selectedVerse = null
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