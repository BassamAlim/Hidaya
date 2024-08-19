package bassamalim.hidaya.features.quranReader.ui

import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.models.QuranPageBookmark
import bassamalim.hidaya.core.models.Verse
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quranReader.domain.QuranReaderDomain
import bassamalim.hidaya.features.quranReader.domain.QuranTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class QuranReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: QuranReaderDomain
): ViewModel() {

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
    private lateinit var suraNames: List<String>
    private val allVerses = domain.getAllVerses()
    var pageNum = when (targetType) {
        QuranTarget.PAGE -> targetValue
        QuranTarget.SURA -> domain.getSuraPageNum(targetValue)
        QuranTarget.VERSE -> domain.getVersePageNum(targetValue)
    }
        private set
    private var suraId = 0
    private var lastClickT = 0L
    private var lastClickedId = -1
    var scrollTo = -1F
        private set
    private val versePositions = mutableMapOf<Int, Float>()

    private val _uiState = MutableStateFlow(QuranReaderUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getViewType(),
        domain.getTextSize(),
        domain.getPageBookmark()
    ) { state, viewType, textSize, pageBookmark -> state.copy(
        pageNum = translateNum(pageNum),
        viewType =
            if (language == Language.ENGLISH) QuranViewType.LIST
            else viewType,
        textSize = textSize,
        isBookmarked = (pageBookmark?.pageNum ?: -1) == pageNum,
        pageVerses = buildPage(pageNum)
    )}.stateIn(
        initialValue = QuranReaderUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    init {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()
            theme = domain.getTheme()

            suraNames = domain.getSuraNames(language)

            _uiState.update { it.copy(
                isTutorialDialogShown = domain.getShouldShowTutorial()
            )}
        }

        if (targetType == QuranTarget.VERSE) {
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
        domain.stopPlayer()
    }

    fun onPageChange(currentPageIdx: Int, pageIdx: Int) {
        if (currentPageIdx == pageIdx) {
            updatePageState(pageIdx + 1) // page number = page index + 1

            domain.handlePageChange(pageIdx)
        }
    }

    fun onBookmarkClick() {
        if (_uiState.value.isBookmarked) {
            viewModelScope.launch {
                domain.setBookmarkedPage(null)
            }
        }
        else {
            viewModelScope.launch {
                domain.setBookmarkedPage(
                    QuranPageBookmark(
                        pageNum = pageNum,
                        suraId = suraId
                    )
                )
            }
        }

        _uiState.update { it.copy(
            isBookmarked = !it.isBookmarked
        )}
    }

    fun onPlayPauseClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            _uiState.update { it.copy(
                isPlayerNotSupportedShown = true
            )}
            return
        }

        if (!domain.isMediaPlayerInitialized() || !domain.isControllerInitialized()) {
            updateButton(PlaybackStateCompat.STATE_BUFFERING)
            domain.setupPlayer(
                controllerCallback = controllerCallback,
                getPageCallback = { pageNum },
                getPageVersesCallback = { _uiState.value.pageVerses },
                getSelectedVerseCallback = { _uiState.value.selectedVerse },
                setSelectedVerseCallback = { value ->
                    _uiState.update { it.copy(
                        selectedVerse = value
                    )}
                }
            )
        }
        else {
            when (domain.getPlaybackState()) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    updateButton(PlaybackStateCompat.STATE_PAUSED)
                    domain.pausePlayer()
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    updateButton(PlaybackStateCompat.STATE_BUFFERING)

                    if (_uiState.value.selectedVerse == null) {
                        domain.playPlayer()

                        _uiState.update { it.copy(
                            selectedVerse = null
                        )}
                    }
                    else
                        domain.requestPlay(_uiState.value.selectedVerse!!.id)
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    updateButton(PlaybackStateCompat.STATE_BUFFERING)

                    if (_uiState.value.selectedVerse == null) {
                        _uiState.update { it.copy(
                            selectedVerse = it.pageVerses[0]
                        )}
                    }

                    domain.requestPlay(_uiState.value.selectedVerse!!.id)
                }
                else -> {}
            }
        }
    }

    fun onPreviousVerseClick() {
        domain.skipToPreviousTrack()
    }

    fun onNextVerseClick() {
        domain.skipToNextTrack()
    }

    fun onSettingsClick() {
        _uiState.update { it.copy(
            isSettingsDialogShown = true
        )}
    }

    fun onVerseClick(verseId: Int, offset: Int) {
        val maxDuration = 1200

        var verse: Verse? = null
        when (_uiState.value.viewType) {
            QuranViewType.PAGE -> {
                val startIdx = _uiState.value.pageVerses.indexOfFirst { it.id == verseId }
                for (idx in startIdx until _uiState.value.pageVerses.size) {
                    val a = _uiState.value.pageVerses[idx]
                    if (offset < a.end) {
                        verse = a
                        break
                    }
                }
            }
            QuranViewType.LIST -> verse = _uiState.value.pageVerses.find { it.id == verseId }
        }

        // double click
        if (verse?.id == lastClickedId
            && System.currentTimeMillis() < lastClickT + maxDuration) {
            _uiState.update { it.copy(
                selectedVerse = null
            )}

            _uiState.update { it.copy(
                isInfoDialogShown = true,
                infoDialogText = verse.interpretation
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

    fun onVerseGloballyPositioned(
        verse: Verse,
        isCurrentPage: Boolean,
        layoutCoordinates: LayoutCoordinates
    ) {
        val screenHeight = domain.getScreenHeight()
        if (isCurrentPage && _uiState.value.viewType == QuranViewType.LIST)
            versePositions[verse.id] = layoutCoordinates.positionInParent().y - screenHeight / 3f
    }

    fun onScrolled() {
        scrollTo = 0F
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            isTutorialDialogShown = false
        )}

        if (doNotShowAgain) {
            viewModelScope.launch {
                domain.setDoNotShowTutorial()
            }
        }
    }

    fun onInfoDialogDismiss() {
        _uiState.update { it.copy(
            isInfoDialogShown = false
        )}
    }

    fun onSettingsDialogDismiss() {
        _uiState.update { it.copy(
            isSettingsDialogShown = false
        )}
    }

    fun setScrollState(scrollState: ScrollState) {
        this.scrollState = scrollState
    }

    fun buildPage(pageNumber: Int): ArrayList<Verse> {
        val pageVerses = ArrayList<Verse>()

        // get page start
        var counter = allVerses.indexOfFirst { verse -> verse.pageNum == pageNumber }
        do {
            val verse = allVerses[counter]
            val suraNum = verse.suraNum // starts from 1
            val verseNum = verse.num

            pageVerses.add(
                Verse(
                    id = verse.id,
                    juzNum = verse.juzNum,
                    suraNum = suraNum,
                    suraName = suraNames[suraNum - 1],
                    num = verseNum,
                    text = "${verse.decoratedText} ",
                    translation = verse.translationEn,
                    interpretation = verse.interpretation
                )
            )

            counter++
        } while (counter != Global.QURAN_VERSES && allVerses[counter].pageNum == pageNumber)

        return pageVerses
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _uiState.update { it.copy(
                trackedVerseId = metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
            )}
            if (_uiState.value.viewType == QuranViewType.LIST) {
                coroutineScope.launch {
                    Log.d(Global.TAG, "Scrolling to ${_uiState.value.trackedVerseId} at ${versePositions[_uiState.value.trackedVerseId]}")
                    if (versePositions[_uiState.value.trackedVerseId] != null)
                        scrollState.animateScrollTo(versePositions[_uiState.value.trackedVerseId]!!.toInt())
                }
            }

            val newPageNum = metadata.getLong("page_num").toInt()
            if (newPageNum != pageNum) {
                viewModelScope.launch {
                    pagerState.scrollToPage(newPageNum - 1)
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
            domain.disconnectMediaBrowser()
        }
    }

    private fun updatePageState(pageNum: Int) {
        suraId = allVerses.first { verse -> verse.pageNum == pageNum }.suraNum - 1
        _uiState.update { it.copy(
            pageNum = translateNum(pageNum),
            suraName = suraNames[suraId],
            juzNum = translateNum(allVerses.first { verse -> verse.pageNum == pageNum }.juzNum),
            pageVerses = buildPage(pageNum),
        )}
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

    private fun translateNum(num: Int) =
        translateNums(
            numeralsLanguage = numeralsLanguage,
            string = num.toString()
        )

}