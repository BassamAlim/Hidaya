package bassamalim.hidaya.features.recitations.recitersMenu.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.recitations.recitersMenu.domain.Recitation
import bassamalim.hidaya.features.recitations.recitersMenu.domain.RecitationRecitersMenuDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
    private lateinit var allRecitations: Flow<List<Recitation>>
    private lateinit var suraNames: List<String>

    private val _uiState = MutableStateFlow(RecitationRecitersMenuUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getNarrationSelections(),
        domain.getLastPlayed()
    ) { state, narrationSelections, lastPlayed ->
        state.copy(
            narrationSelections = narrationSelections,
            lastPlayedMedia = domain.getLastPlayedMedia(lastPlayed.mediaId)
        )
    }.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = RecitationRecitersMenuUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()
            suraNames = domain.getSuraNames(language)
            allRecitations = domain.observeRecitersWithNarrations(language)

            domain.cleanFiles()

            _uiState.update { it.copy(
                isLoading = false,
                isFiltered = it.narrationSelections.values.any { bool -> !bool }
            )}
        }
    }

    fun onStart() {
        domain.registerDownloadReceiver()
    }

    fun onStop() {
        domain.unregisterDownloadReceiver()
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

    fun getItems(page: Int): Flow<List<Recitation>> {
        val menuType = MenuType.entries[page]

        return allRecitations.map { recitations ->
            val all = recitations.map { recitation ->
                Recitation(
                    reciterId = recitation.reciterId,
                    reciterName = recitation.reciterName,
                    isFavoriteReciter = recitation.isFavoriteReciter,
                    narrations = recitation.narrations.map { narration ->
                        Recitation.Narration(
                            id = narration.id,
                            name = narration.name,
                            server = narration.server,
                            availableSuras = narration.availableSuras,
                            downloadState = narration.downloadState
                        )
                    }
                )
            }

            val filtered = all.filter { recitation ->
                recitation.narrations.any { narration ->
                    _uiState.value.narrationSelections[narration.id]!!
                }
            }.map { recitation ->
                recitation.copy(
                    narrations = recitation.narrations.filter { narration ->
                        _uiState.value.narrationSelections[narration.id]!!
                    }
                )
            }

            val items = when (menuType) {
                MenuType.FAVORITES -> {
                    filtered.filter { recitation -> recitation.isFavoriteReciter }
                }
                MenuType.DOWNLOADED -> {
                    val hasDownloaded = filtered.filter { recitation ->
                        recitation.narrations.any { narration ->
                            narration.downloadState == DownloadState.DOWNLOADED
                        }
                    }
                    hasDownloaded.map {  recitation ->
                        recitation.copy(narrations = recitation.narrations.filter { narration ->
                            narration.downloadState == DownloadState.DOWNLOADED
                        })
                    }
                }
                else -> filtered
            }

            if (_uiState.value.searchText.isEmpty()) items
            else items.filter { reciter ->
                reciter.reciterName.contains(_uiState.value.searchText, true)
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
        viewModelScope.launch {
            if (allRecitations.first()[reciterId].narrations[narration.id].downloadState
                == DownloadState.NOT_DOWNLOADED) {
                domain.downloadNarration(
                    reciterId = reciterId,
                    narration = narration,
                    suraNames = suraNames,
                    language = language,
                    suraString = suraString
                )
            }
            else domain.deleteNarration(reciterId, narration)
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