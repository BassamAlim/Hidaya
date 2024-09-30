package bassamalim.hidaya.features.quran.reader.ui

import android.app.Activity
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
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
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quran.reader.domain.QuranReaderDomain
import bassamalim.hidaya.features.quran.reader.domain.QuranTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import bassamalim.hidaya.core.data.dataSources.room.entities.Verse as VerseEntity

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class QuranReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: QuranReaderDomain,
    private val navigator: Navigator
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
    private lateinit var allVerses: List<VerseEntity>
    var pageNum = 0
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
    ) { state, viewType, textSize, pageBookmark ->
        if (state.isLoading) return@combine state

        state.copy(
            pageNum = translateNums(
                string = pageNum.toString(),
                numeralsLanguage = numeralsLanguage
            ),
            viewType =
                if (language == Language.ENGLISH) QuranViewType.LIST
                else viewType,
            textSize = textSize,
            isBookmarked = (pageBookmark?.pageNum ?: -1) == pageNum,
        )
    }.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QuranReaderUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()
            theme = domain.getTheme()

            allVerses = domain.getAllVerses()
            suraNames = domain.getSuraNames(language)

            pageNum = when (targetType) {
                QuranTarget.PAGE -> targetValue
                QuranTarget.SURA -> domain.getSuraPageNum(targetValue)
                QuranTarget.VERSE -> domain.getVersePageNum(targetValue)
            }

            domain.setPageNumCallback { pageNum }

            _uiState.update { it.copy(
                isLoading = false,
                isTutorialDialogShown = domain.getShouldShowTutorial()
            )}
        }

        if (targetType == QuranTarget.VERSE) {
            _uiState.update { it.copy(
                selectedVerse = it.pageVerses.first { verse -> verse.id == targetValue }
            )}
        }
    }

    fun onStart(pagerState: PagerState, coroutineScope: CoroutineScope) {
        this.pagerState = pagerState
        this.coroutineScope = coroutineScope
    }

    fun onStop(activity: Activity) {
        domain.stopPlayer(activity)
    }

    fun onPageChange(currentPageIdx: Int, pageIdx: Int, scrollState: ScrollState) {
        if (currentPageIdx == pageIdx) {
            pageNum = pageIdx+1

            suraId = allVerses.first { verse -> verse.pageNum == pageNum }.suraNum - 1
            _uiState.update { it.copy(
                pageNum = translateNums(
                    string = pageNum.toString(),
                    numeralsLanguage = numeralsLanguage
                ),
                suraName = suraNames[suraId],
                juzNum = translateNums(
                    string = allVerses.first { verse ->
                        verse.pageNum == pageNum
                    }.juzNum.toString(),
                    numeralsLanguage = numeralsLanguage
                ),
                pageVerses = buildPage(pageNum)
            )}

            domain.handlePageChange(pageNum)

            this.scrollState = scrollState
        }
    }

    fun onBookmarkClick(isBookmarked: Boolean) {
        viewModelScope.launch {
            if (isBookmarked) domain.setBookmarkedPage(null)
            else domain.setBookmarkedPage(QuranPageBookmark(pageNum = pageNum, suraId = suraId))
        }
    }

    fun onPlayPauseClick(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            _uiState.update { it.copy(
                isPlayerNotSupportedShown = true
            )}
            return
        }

        if (!domain.isMediaPlayerInitialized() || !domain.isControllerInitialized()) {
            updateButton(PlaybackStateCompat.STATE_BUFFERING)
            domain.setupPlayer(
                activity = activity,
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
        navigator.navigate(Screen.QuranSettings)
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
            if (_uiState.value.selectedVerse?.id == verse!!.id) {
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

        lastClickedId = verse.id
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

    fun buildPage(pageNumber: Int): List<Verse> {
        val pageVerses = mutableListOf<Verse>()

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
        } while (counter != Global.NUM_OF_QURAN_VERSES && allVerses[counter].pageNum == pageNumber)

        return pageVerses
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _uiState.update { it.copy(
                trackedVerseId = metadata
                    .getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER).toInt()
            )}
            if (_uiState.value.viewType == QuranViewType.LIST) {
                coroutineScope.launch {
                    if (versePositions[_uiState.value.trackedVerseId] != null)
                        scrollState.animateScrollTo(
                            versePositions[_uiState.value.trackedVerseId]!!.toInt()
                        )
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