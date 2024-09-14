package bassamalim.hidaya.features.recitations.recitersMenu.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.models.Recitation
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.quran.surasMenu.ui.LastPlayedMedia
import bassamalim.hidaya.features.recitations.recitersMenu.domain.RecitationRecitersMenuDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecitationRecitersMenuViewModel @Inject constructor(
    private val domain: RecitationRecitersMenuDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var language: Language
    private var continueListeningMediaId = ""
    private lateinit var allNarrations: List<Recitation.Narration>
    private lateinit var allRecitations: List<Recitation>
    lateinit var narrationOptions: List<String>
    private lateinit var suraNames: List<String>

    private val _uiState = MutableStateFlow(RecitationRecitersMenuUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getNarrationSelections(),
        domain.getLastPlayed()
    ) { state, narrationSelections, lastPlayed ->
        state.copy(
            narrationSelections = narrationSelections,
            lastPlayedMedia = getLastPlayedMedia(lastPlayed.mediaId)
        )
    }.stateIn(
        initialValue = RecitationRecitersMenuUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    init {
        viewModelScope.launch {
            language = domain.getLanguage()

            suraNames = domain.getSuraNames(language)
            allNarrations = domain.getAllNarrations(language)
            narrationOptions = allNarrations.map { it.name }.distinct()
            allRecitations = domain.getAllRecitations(language)
        }

        _uiState.update { it.copy(
            isFiltered = it.narrationSelections.values.any { bool -> !bool }
        )}
    }

    fun onStart() {
        domain.cleanFiles()

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(
                downloadStates = getDownloadStates()
            )}
        }

        domain.registerDownloadReceiver(onComplete)
    }

    fun onStop() {
        domain.unregisterDownloadReceiver(onComplete)
    }

    fun onBackPressed() {
        val context = navigator.getContext()
        if ((context as Activity).isTaskRoot) {
            navigator.navigate(Screen.Main) {
                popUpTo(Screen.RecitationsRecitersMenu.route) {
                    inclusive = true
                }
            }
        }
        else (context as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
    }

    private fun getDownloadStates(): Map<Int, DownloadState> {
        val downloadStates = mutableMapOf<Int, DownloadState>()

        for (recitation in allRecitations) {
            for (narration in recitation.narrations) {
                downloadStates[recitation.reciterId] =
                    if (domain.checkIsDownloaded(
                            reciterId = recitation.reciterId,
                            narrationId = narration.id
                        ))
                        DownloadState.DOWNLOADED
                    else
                        DownloadState.NOT_DOWNLOADED
            }
        }

        return downloadStates
    }

    private suspend fun getLastPlayedMedia(mediaId: String): LastPlayedMedia? {
        if (mediaId.isEmpty() || mediaId == "00000000") return null  // added the second part to prevent errors due to change in db
        Log.d("RecitationsRecitersMenuViewModel", "continueListeningMediaId: $mediaId")

        val reciterId = mediaId.substring(0, 3).toInt()
        val narrationId = mediaId.substring(3, 5).toInt()
        val suraId = mediaId.substring(5).toInt()
        Log.d(
            "RecitationsRecitersMenuViewModel",
            "reciterId: $reciterId, narrationId: $narrationId, suraIndex: $suraId"
        )

        return LastPlayedMedia(
            reciterName = domain.getReciterName(reciterId, language),
            suraName = suraNames[suraId],
            narrationName = domain.getNarration(reciterId, narrationId).nameAr
        )
    }

    fun getItems(page: Int): Flow<List<Recitation>> {
        val menuType = MenuType.entries[page]

        val recitersFlow = domain.observeAllReciters(language)
        return recitersFlow.map { reciters ->
            val items = reciters.filter { reciter ->
                !(menuType == MenuType.FAVORITES && !reciter.isFavorite)
            }.map { reciter ->
                val narrations = filterSelectedNarrations(
                    domain.getReciterNarrations(reciter.id, language)
                )
                val narrationsList = narrations.filter { narration ->
                    domain.checkIsDownloaded(reciter.id, narrationId = narration.id)
                }.map { narration ->
                    Recitation.Narration(
                        id = narration.id,
                        server = narration.server,
                        name = narration.name,
                        availableSuras = narration.availableSuras
                    )
                }

                Recitation(
                    reciterId = reciter.id,
                    reciterName = reciter.name,
                    narrations = narrationsList,
                    reciterIsFavorite = reciter.isFavorite
                )
            }.filter {
                it.narrations.isNotEmpty()
            }

            if (_uiState.value.searchText.isEmpty()) items
            else items.filter { reciter ->
                reciter.reciterName.contains(_uiState.value.searchText, true)
            }
        }
    }

    private fun filterSelectedNarrations(
        narrations: List<Recitation.Narration>
    ): List<Recitation.Narration> {
        if (!_uiState.value.isFiltered) return narrations

        val selected = mutableListOf<Recitation.Narration>()
        for (i in narrations.indices) {
            for (j in this.allNarrations.indices) {
                if (_uiState.value.narrationSelections[j]!!
                    && narrations[i].name.startsWith(this.allNarrations[j].name)) {
                    selected.add(narrations[i])
                    break
                }
            }
        }

        return selected
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            try {
                val narrationId = domain.getDownloadingNarrationId(downloadId)
                _uiState.update { it.copy(
                    downloadStates = it.downloadStates.toMutableMap().apply {
                        this[narrationId] = DownloadState.DOWNLOADED
                    }
                )}

                domain.removeDownloading(downloadId)
            } catch (e: RuntimeException) {
                _uiState.update { it.copy(
                    downloadStates = getDownloadStates()
                )}
            }
        }
    }


    fun onContinueListeningClick() {
        if (continueListeningMediaId.isNotEmpty()) {
            navigator.navigate(
                Screen.RecitationPlayer(
                    action = "continue",
                    mediaId = continueListeningMediaId
                )
            )
        }
    }

    fun onFilterClick() {
        _uiState.update { it.copy(
            filterDialogShown = true
        )}
    }

    fun onFilterDialogDismiss(selections: Map<Int, Boolean>) {
        _uiState.update { it.copy(
            filterDialogShown = false,
            narrationSelections = selections
        )}
    }

    fun onFavoriteClick(reciterId: Int, oldValue: Boolean) {
        viewModelScope.launch {
            domain.setFavorite(reciterId, !oldValue)
        }
    }

    fun onDownloadNarrationClick(
        reciterId: Int,
        narration: Recitation.Narration,
        suraString: String
    ) {
        if (_uiState.value.downloadStates[reciterId] == DownloadState.NOT_DOWNLOADED) {
            _uiState.update { it.copy(
                downloadStates = it.downloadStates.toMutableMap().apply {
                    this[reciterId] = DownloadState.DOWNLOADING
                }
            )}

            viewModelScope.launch {
                domain.downloadNarration(
                    reciterId = reciterId,
                    narration = narration,
                    suraNames = suraNames,
                    language = language,
                    suraString = suraString
                )
            }
        }
        else {
            _uiState.update { it.copy(
                downloadStates = it.downloadStates.toMutableMap().apply {
                    this[narration.id] = DownloadState.NOT_DOWNLOADED
                }
            )}

            domain.deleteNarration(reciterId, narration)
        }
    }

    fun onNarrationClick(reciterId: Int, narrationId: Int) {
        navigator.navigate(
            Screen.RecitationSurasMenu(
                reciterId = reciterId.toString(),
                narrationId = narrationId.toString()
            )
        )
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}