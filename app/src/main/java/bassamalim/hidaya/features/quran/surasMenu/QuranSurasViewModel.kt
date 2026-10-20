package bassamalim.hidaya.features.quran.surasMenu

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import bassamalim.hidaya.core.data.dataSources.room.entities.Verse
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.enums.PlaybackStatus
import bassamalim.hidaya.core.helpers.PlayerConnection
import bassamalim.hidaya.core.helpers.playbackStatus
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quran.reader.QuranTarget
import bassamalim.hidaya.features.recitations.RecitationMediaId
import bassamalim.hidaya.features.recitations.SuraDownloads
import bassamalim.hidaya.features.recitations.player.RecitationPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class QuranSurasViewModel @Inject constructor(
    private val domain: QuranSurasDomain,
    private val suraDownloads: SuraDownloads,
    private val navigator: Navigator
): ViewModel() {

    /**
     * Holds all data loaded asynchronously in [initializeData]. Kept as a single nullable holder
     * set atomically once loading completes, so callbacks can never observe a partially
     * initialized state (previously the source of [UninitializedPropertyAccessException] crashes
     * when a callback fired before the loading coroutine finished).
     */
    private data class LoadedData(
        val numeralsLanguage: Language,
        val suraNames: List<String>,
        val allSurasFlow: Flow<List<Sura>>,
        val allSuras: List<Sura>,
        val allVerses: List<Verse>,
        /** Keyed by 0-based sura id */
        val suraStartPages: Map<Int, Int>
    )

    private var data: LoadedData? = null

    private val _uiState = MutableStateFlow(QuranSurasUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getBookmarks()
    ) { state, bookmarks ->
        val data = data
        if (state.isLoading || data == null) state
        else state.copy(
            bookmarks = listOf(
                bookmarks.bookmark1VerseId,
                bookmarks.bookmark2VerseId,
                bookmarks.bookmark3VerseId,
                bookmarks.bookmark4VerseId
            ).mapIndexedNotNull { index, verseId ->
                val verse = data.allVerses.firstOrNull { it.id == verseId }
                    ?: return@mapIndexedNotNull null
                BookmarkItem(
                    index = index,
                    verseId = verse.id,
                    suraName = data.suraNames[verse.suraNum - 1],
                    verseNumText = translateNums(
                        string = verse.num.toString(),
                        numeralsLanguage = data.numeralsLanguage
                    )
                )
            }
        )
    }.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QuranSurasUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            val language = domain.getLanguage()
            val allSurasFlow = domain.getAllSuras(language)
            val allVerses = domain.getAllVerses()
            data = LoadedData(
                numeralsLanguage = domain.getNumeralsLanguage(),
                suraNames = domain.getSuraNames(language),
                allSurasFlow = allSurasFlow,
                allSuras = allSurasFlow.first(),
                allVerses = allVerses,
                suraStartPages = allVerses.groupBy { it.suraNum - 1 }
                    .mapValues { (_, verses) -> verses.minOf { it.pageNum } }
            )

            _uiState.update { it.copy(
                isLoading = false,
                isTutorialActive = domain.getShouldShowTutorial()
            )}
        }
    }

    fun onTutorialFinished() {
        _uiState.update { it.copy(isTutorialActive = false) }
        viewModelScope.launch {
            domain.setTutorialSeen()
        }
    }

    fun onSuraClick(suraId: Int) {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.SURA,
                targetValue = suraId
            )
        )

        data?.let { domain.trackSuraViewed(it.suraNames[suraId]) }
    }

    private var connection: PlayerConnection? = null

    /** What the player has loaded, and what was last played and may be continued. */
    private var loadedRecitation: RecitationInfo? = null
    private var lastPlayedRecitation: RecitationInfo? = null

    init {
        viewModelScope.launch {
            // The position is saved every few seconds while playing; only the recitation matters
            domain.getLastPlayedRecitation()
                .map { lastPlayed -> lastPlayed?.takeIf { !it.isDismissed }?.mediaId }
                .distinctUntilChanged()
                .collect { mediaId ->
                    lastPlayedRecitation = mediaId?.let { domain.getRecitationInfo(it) }
                    updatePlaybackBar()
                }
        }

        // Keeps the open picker's download buttons current
        viewModelScope.launch {
            suraDownloads.changes.collect {
                _uiState.update { it.copy(reciterPicker = it.reciterPicker?.withDownloadStates()) }
            }
        }
    }

    fun onStart(context: Context) {
        // Connecting starts the service, so it waits for a recitation to have been started.
        // That only happens on the player screen, and coming back from it runs this again
        if (RecitationPlayerService.isRunning) {
            connection = PlayerConnection(
                context = context.applicationContext,
                service = RecitationPlayerService::class.java,
                onChange = ::onPlayerChange
            )
        }
        else {
            loadedRecitation = null
            _uiState.update { it.copy(playbackState = PlaybackStatus.STOPPED) }
            updatePlaybackBar()
        }

        suraDownloads.register()
    }

    fun onStop() {
        connection?.release()
        connection = null

        suraDownloads.unregister()
    }

    private fun onPlayerChange(controller: MediaController) {
        val metadata = controller.currentMediaItem?.mediaMetadata

        loadedRecitation = metadata?.let { m ->
            RecitationInfo(
                reciterName = m.artist?.toString() ?: "",
                narrationName = m.albumTitle?.toString() ?: "",
                suraName = m.title?.toString() ?: ""
            )
        }
        _uiState.update { it.copy(playbackState = controller.playbackStatus()) }
        updatePlaybackBar()
    }

    // With nothing loaded (e.g. after the app restarted), the bar offers the last played instead
    private fun updatePlaybackBar() {
        _uiState.update { it.copy(
            playbackRecitationInfo = loadedRecitation ?: lastPlayedRecitation
        )}
    }

    fun onPlaybackBarClick() {
        val mediaId = connection?.controller?.currentMediaItem?.mediaId
        if (mediaId != null)
            navigator.navigate(Screen.RecitationPlayer(action = "back", mediaId = mediaId))
        else continueLastPlayed()
    }

    fun onPlayPauseClick() {
        val controller = connection?.controller
        if (controller?.currentMediaItem == null) continueLastPlayed()
        else Util.handlePlayPauseButtonAction(controller)
    }

    private fun continueLastPlayed() {
        viewModelScope.launch {
            val lastPlayed = domain.getLastPlayedRecitation().first() ?: return@launch
            navigator.navigate(
                Screen.RecitationPlayer(action = "continue", mediaId = lastPlayed.mediaId)
            )
        }
    }

    /** Hides the bar until something is played again, unloading what the player holds. */
    fun onPlaybackBarDismiss() {
        connection?.controller?.let { controller ->
            controller.stop()
            controller.clearMediaItems()
        }

        lastPlayedRecitation = null
        updatePlaybackBar()
        viewModelScope.launch { domain.dismissLastPlayedRecitation() }
    }

    fun onSuraPlayClick(suraId: Int) {
        val data = data ?: return

        viewModelScope.launch {
            val (lastPlayed, favorites) = domain.getSuraRecitations(suraId)
            // No quick picks to offer, so go straight to choosing a reciter
            if (lastPlayed == null && favorites.isEmpty()) return@launch onAllRecitersClick(suraId)

            _uiState.update { it.copy(
                reciterPicker = ReciterPicker(
                    suraId = suraId,
                    suraName = data.suraNames[suraId],
                    lastPlayed = lastPlayed,
                    favorites = favorites
                ).withDownloadStates()
            )}
        }
    }

    private fun ReciterPicker.withDownloadStates(): ReciterPicker {
        fun SuraRecitation.withDownloadState() = copy(
            downloadState = suraDownloads.getState(reciterId, narrationId, suraId)
        )

        return copy(
            lastPlayed = lastPlayed?.withDownloadState(),
            favorites = favorites.map { it.withDownloadState() }
        )
    }

    fun onSuraDownloadClick(suraId: Int, recitation: SuraRecitation) {
        val suraName = data?.allSuras?.firstOrNull { it.id == suraId }?.plainName ?: return

        suraDownloads.toggle(
            reciterId = recitation.reciterId,
            narrationId = recitation.narrationId,
            suraId = suraId,
            suraName = suraName,
            server = recitation.server
        )
    }

    fun onAllRecitersClick(suraId: Int) {
        onReciterPickerDismiss()
        navigator.navigate(Screen.RecitationsRecitersMenu(suraId))
    }

    fun onReciterPickerDismiss() {
        _uiState.update { it.copy(reciterPicker = null) }
    }

    fun onSuraRecitationClick(suraId: Int, recitation: SuraRecitation) {
        onReciterPickerDismiss()
        playRecitation(recitation.reciterId, recitation.narrationId, suraId)
    }

    private fun playRecitation(reciterId: Int, narrationId: Int, suraId: Int) {
        navigator.navigate(
            Screen.RecitationPlayer(
                action = "start",
                mediaId = RecitationMediaId.encode(reciterId, narrationId, suraId)
            )
        )

        data?.allSuras?.firstOrNull { it.id == suraId }?.let { sura ->
            domain.trackRecitationPlayed(reciterId, narrationId, sura.plainName)
        }
    }

    fun onPageClick(pageNum: Int) {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.PAGE,
                targetValue = pageNum
            )
        )
    }

    fun onBookmarkClick(verseId: Int) {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.VERSE,
                targetValue = verseId
            )
        )
    }

    fun onFavoriteClick(itemId: Int, oldState: Boolean) {
        viewModelScope.launch {
            domain.setFav(suraId = itemId, fav = !oldState)
        }
    }

    fun getItems(page: Int): Flow<List<SuraItem>> {
        val data = data ?: return flowOf(emptyList())
        val menuType = MenuType.entries[page]

        return data.allSurasFlow.map { suras ->
            suras.filter { sura ->
                !(menuType == MenuType.FAVORITES && !sura.isFavorite)
            }.map { sura ->
                SuraItem(
                    id = sura.id,
                    name = data.suraNames[sura.id],
                    numberText = translateNums(
                        string = (sura.id + 1).toString(),
                        numeralsLanguage = data.numeralsLanguage
                    ),
                    // Revelation 0 is Meccan, 1 is Medinan
                    isMeccan = sura.revelation == 0,
                    startPageText = translateNums(
                        string = data.suraStartPages.getValue(sura.id).toString(),
                        numeralsLanguage = data.numeralsLanguage
                    ),
                    isFavorite = sura.isFavorite
                )
            }
        }
    }

    fun searchSurasAndPages(query: String): List<SearchMatch> {
        val data = data ?: return emptyList()

        return if (query.isEmpty()) {
            data.allSuras.take(3).map { sura ->
                SuraMatch(
                    id = sura.id,
                    decoratedName = data.suraNames[sura.id],
                    plainName = sura.plainName,
                    isFavorite = sura.isFavorite
                )
            }
        }
        else if (query.isDigitsOnly()) {
            val num = query.toInt()
            if (num in 1..604) {
                val pageSuraId = data.allVerses.first { verse -> verse.pageNum == num }.suraNum - 1
                listOf(
                    PageMatch(
                        page = num,
                        num = translateNums(
                            string = query,
                            numeralsLanguage = data.numeralsLanguage
                        ),
                        suraName = data.suraNames[pageSuraId]
                    )
                )
            }
            else emptyList()
        }
        else {
            domain.searchSuras(
                query = query,
                items = data.allSuras,
                limit = 3
            )
        }
    }

    fun searchVerses(query: String, highlightColor: Color): List<VerseMatch> {
        val data = data ?: return emptyList()

        return domain.searchVerses(
            query = query,
            items = data.allVerses,
            suraNames = data.suraNames,
            numeralsLanguage = data.numeralsLanguage,
            highlightColor = highlightColor
        )
    }

    fun onVerseClick(verseId: Int) {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.VERSE,
                targetValue = verseId
            )
        )
    }

}