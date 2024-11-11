package bassamalim.hidaya.features.quran.reader.ui

import android.app.Activity
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var pagerState: PagerState
    private lateinit var scrollState: ScrollState
    private lateinit var suraNames: List<String>
    private lateinit var allVerses: List<VerseEntity>
    var pageNum = 0
        private set
    private var suraId = 0
    var scrollTo = -1F
        private set
    private val versePositions = mutableMapOf<Int, Float>()
    private var shouldSelectVerse = false
    private var pressedVerseId: Int? = null
    private var longPressJob: Job? = null

    private val _uiState = MutableStateFlow(QuranReaderUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getViewType(),
        domain.getFillPage(),
        domain.getTextSize(),
        domain.getPageBookmark()
    ) { state, viewType, fillPage, textSize, pageBookmark ->
        if (state.isLoading) return@combine state

        state.copy(
            pageNum = translateNums(
                string = pageNum.toString(),
                numeralsLanguage = numeralsLanguage
            ),
            viewType = if (language == Language.ARABIC) viewType else QuranViewType.LIST,
            fillPage = fillPage,
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

            allVerses = domain.getAllVerses()
            suraNames = domain.getSuraNames(language)

            pageNum = when (targetType) {
                QuranTarget.PAGE -> targetValue
                QuranTarget.SURA -> domain.getSuraPageNum(targetValue)
                QuranTarget.VERSE -> {
                    shouldSelectVerse = true
                    domain.getVersePageNum(targetValue)
                }
            }

            domain.setPageNumCallback { pageNum }

            _uiState.update { it.copy(
                isLoading = false,
                isTutorialDialogShown = domain.getShouldShowTutorial()
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
                pageVerses = getPageVerses(pageNum)
            )}

            if (shouldSelectVerse) {
                _uiState.update { it.copy(
                    selectedVerse = it.pageVerses.first { verse -> verse.id == targetValue }
                )}
                shouldSelectVerse = false
            }

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

    fun onVersePointerInput(
        pointerInputScope: PointerInputScope,
        layoutResult: TextLayoutResult?,
        annotatedString: AnnotatedString
    ) {
        val swipeThreshold = 10f

        viewModelScope.launch {
            pointerInputScope.awaitPointerEventScope {
                var initialPosition: Offset? = null
                var verseId: Int? = null
                var isLongPressDetected = false

                while (true) {
                    val event = awaitPointerEvent()
                    val offset = event.changes[0].position

                    when (event.type) {
                        PointerEventType.Press -> {
                            longPressJob?.cancel()
                            isLongPressDetected = false

                            initialPosition = offset
                            val position = layoutResult?.getOffsetForPosition(offset)
                            verseId = position?.let {
                                annotatedString.getStringAnnotations(start = position, end = position)
                                    .firstOrNull()?.item
                                    ?.substringAfter('=')
                                    ?.substringBefore(')')
                                    ?.toInt()
                            }

                            verseId?.let { onVersePressed(it) }
                        }
                        PointerEventType.Release -> {
                            val movementDistance = initialPosition?.let { start ->
                                (offset - start).getDistance()
                            } ?: 0f

                            if (movementDistance <= swipeThreshold && !isLongPressDetected)
                                verseId?.let { onVerseReleased() }

                            initialPosition = null
                            verseId = null
                            isLongPressDetected = false
                        }
                        PointerEventType.Move -> {
                            val movementDistance = initialPosition?.let { start ->
                                (offset - start).getDistance()
                            } ?: 0f

                            if (movementDistance > swipeThreshold) {
                                isLongPressDetected = true
                                longPressJob?.cancel()
                                pressedVerseId = null
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onVersePressed(verseId: Int) {
        pressedVerseId = verseId

        longPressJob = coroutineScope.launch {
            delay(500)
            if (pressedVerseId == verseId) onVerseHold(verseId)
        }
    }

    private fun onVerseReleased() {
        pressedVerseId?.let { onVerseClick(it) }
        pressedVerseId = null
        longPressJob?.cancel()
    }

    private fun onVerseClick(verseId: Int) {
        val verse = _uiState.value.pageVerses.find { it.id == verseId }!!

        _uiState.update { it.copy(
            selectedVerse = if (_uiState.value.selectedVerse?.id == verse.id) null else verse
        )}
    }

    private fun onVerseHold(verseId: Int) {
        navigator.navigate(Screen.VerseInfo(verseId.toString()))
        pressedVerseId = null
        longPressJob?.cancel()
    }

    fun onSuraHeaderGloballyPositioned(
        suraNum: Int,
        isCurrentPage: Boolean,
        layoutCoordinates: LayoutCoordinates
    ) {
        if (
            isCurrentPage
            && scrollTo == -1F
            && targetType == QuranTarget.SURA
            && suraNum == targetValue+1
            ) {
            scrollTo = layoutCoordinates.positionInParent().y - 13
        }
    }

    fun onVerseGloballyPositioned(
        verseId: Int,
        isCurrentPage: Boolean,
        layoutCoordinates: LayoutCoordinates
    ) {
        val screenHeight = domain.getScreenHeight()
        if (isCurrentPage && _uiState.value.viewType == QuranViewType.LIST)
            versePositions[verseId] = layoutCoordinates.positionInParent().y - screenHeight / 3f
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

    private fun getPageVerses(pageNumber: Int) =
        allVerses.filter { it.pageNum == pageNumber }.map { verse ->
            Verse(
                id = verse.id,
                juzNum = verse.juzNum,
                suraNum = verse.suraNum,
                suraName = suraNames[verse.suraNum - 1],
                num = verse.num,
                text = "${verse.decoratedText} ",
                startLineNum = verse.startLineNum,
                endLineNum = verse.endLineNum,
                translation = verse.translationEn,
                interpretation = verse.interpretation
            )
        }

    fun buildPage(
        pageNumber: Int,
        defaultVerseColor: Color,
        selectedVerseColor: Color,
        trackedVerseColor: Color
    ): List<Section> {
        val sections = mutableListOf<Section>()

        val tempVerses = mutableListOf<Verse>()
        // get page start
        var counter = allVerses.indexOfFirst { verse -> verse.pageNum == pageNumber }
        do {
            val verse = allVerses[counter]

            if (verse.num == 1) {
                if (tempVerses.isNotEmpty()) {
                    sections.add(
                        VersesSection(
                            annotatedString = versesToAnnotatedString(
                                verses = tempVerses.toList(),
                                defaultVerseColor = defaultVerseColor,
                                selectedVerseColor = selectedVerseColor,
                                trackedVerseColor = trackedVerseColor
                            ),  // toList() to make a copy
                            numOfLines =
                            tempVerses.last().endLineNum - tempVerses.first().startLineNum + 1
                        )
                    )
                    tempVerses.clear()
                }

                sections.add(
                    SuraHeaderSection(
                        suraNum = verse.suraNum,
                        suraName = suraNames[verse.suraNum - 1]
                    )
                )

                if (verse.suraNum != 1 && verse.suraNum != 9)
                    sections.add(BasmalahSection())
            }

            tempVerses.add(
                Verse(
                    id = verse.id,
                    juzNum = verse.juzNum,
                    suraNum = verse.suraNum,
                    suraName = suraNames[verse.suraNum - 1],
                    num = verse.num,
                    text = "${verse.decoratedText} ",
                    startLineNum = verse.startLineNum,
                    endLineNum = verse.endLineNum,
                    translation = verse.translationEn,
                    interpretation = verse.interpretation
                )
            )

            counter++
        } while (counter != Global.NUM_OF_QURAN_VERSES && allVerses[counter].pageNum == pageNumber)

        if (tempVerses.isNotEmpty()) {
            sections.add(
                VersesSection(
                    annotatedString = versesToAnnotatedString(
                        verses = tempVerses.toList(),
                        defaultVerseColor = defaultVerseColor,
                        selectedVerseColor = selectedVerseColor,
                        trackedVerseColor = trackedVerseColor
                    ),  // toList() to make a copy
                    numOfLines = tempVerses.last().endLineNum - tempVerses.first().startLineNum + 1
                )
            )
            tempVerses.clear()
        }

        return sections
    }

    fun buildListPage(
        pageNumber: Int,
        defaultVerseColor: Color,
        selectedVerseColor: Color,
        trackedVerseColor: Color
    ): List<Section> {
        val sections = mutableListOf<Section>()

        // get page start
        var counter = allVerses.indexOfFirst { verse -> verse.pageNum == pageNumber }
        do {
            val verse = allVerses[counter]

            if (verse.num == 1) {
                sections.add(
                    SuraHeaderSection(
                        suraNum = verse.suraNum,
                        suraName = suraNames[verse.suraNum - 1]
                    )
                )

                if (verse.suraNum != 1 && verse.suraNum != 9)
                    sections.add(BasmalahSection())
            }

            sections.add(
                ListVerse(
                    id = verse.id,
                    text = versesToAnnotatedString(
                        listOf(
                            Verse(
                                id = verse.id,
                                juzNum = verse.juzNum,
                                suraNum = verse.suraNum,
                                suraName = suraNames[verse.suraNum - 1],
                                num = verse.num,
                                text = "${verse.decoratedText} ",
                                startLineNum = verse.startLineNum,
                                endLineNum = verse.endLineNum,
                                translation = verse.translationEn,
                                interpretation = verse.interpretation
                            )
                        ),
                        defaultVerseColor = defaultVerseColor,
                        selectedVerseColor = selectedVerseColor,
                        trackedVerseColor = trackedVerseColor
                    ),
                    translation = verse.translationEn
                )
            )

            counter++
        } while (counter != Global.NUM_OF_QURAN_VERSES && allVerses[counter].pageNum == pageNumber)

        return sections
    }

    private fun versesToAnnotatedString(
        verses: List<Verse>,
        defaultVerseColor: Color,
        selectedVerseColor: Color,
        trackedVerseColor: Color
    ): AnnotatedString {
        val selectedVerseId = _uiState.value.selectedVerse?.id
        val trackedVerseId = _uiState.value.trackedVerseId

        return buildAnnotatedString {
            for (verse in verses) {
                val verseColor = when (verse.id) {
                    selectedVerseId -> selectedVerseColor
                    trackedVerseId -> trackedVerseColor
                    else -> defaultVerseColor
                }

                pushStringAnnotation(tag = verse.id.toString(), annotation = verse.id.toString())
                withStyle(style = SpanStyle(color = verseColor)) {
                    append(verse.text)
                }
                pop()
            }
        }
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