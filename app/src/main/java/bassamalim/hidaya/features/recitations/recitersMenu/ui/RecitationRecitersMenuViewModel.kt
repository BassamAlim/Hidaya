package bassamalim.hidaya.features.recitations.recitersMenu.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    private var lastPlayedMediaId: String? = null
    private lateinit var allRecitations: Flow<Map<Int, Recitation>>
    private lateinit var suraNames: List<String>
    private lateinit var narrationSelections: Flow<Map<String, Boolean>>
    var searchText by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(RecitationRecitersMenuUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getLastPlayed()
    ) { state, lastPlayed ->
        if (state.isLoading) return@combine state

        lastPlayed?.let { lastPlayedMediaId = it.mediaId }

        state.copy(
            lastPlayedMedia = lastPlayed?.mediaId?.let { domain.getLastPlayedMedia(it) },
            isFiltered = narrationSelections.first().values.any { bool -> !bool }
        )
    }.onStart {
        initializeData()
        domain.cleanFiles()
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
            narrationSelections = domain.getNarrationSelections(language)

            _uiState.update { it.copy(
                isLoading = false
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

        return combine(allRecitations, narrationSelections) { allRecitations, narrationSelections ->
            val items = when (menuType) {
                MenuType.FAVORITES -> {
                    allRecitations.filter { recitation -> recitation.value.isFavoriteReciter }
                }
                MenuType.DOWNLOADED -> {
                    val hasDownloaded = allRecitations.filter { recitation ->
                        recitation.value.narrations.any { narration ->
                            narration.value.downloadState == DownloadState.DOWNLOADED
                        }
                    }
                    hasDownloaded.map { recitation ->
                        recitation.key to recitation.value.copy(
                            narrations = recitation.value.narrations.filter { narration ->
                                narration.value.downloadState == DownloadState.DOWNLOADED
                            }
                        )
                    }.toMap()
                }
                else -> allRecitations
            }

            val selectedItems = items.values.filter { recitation ->
                recitation.narrations.any { narration ->
                    narrationSelections[narration.value.name]!!
                }
            }.map { recitation ->
                recitation.copy(
                    narrations = recitation.narrations.filter { narration ->
                        narrationSelections[narration.value.name]!!
                    }
                )
            }.filter { recitation -> recitation.narrations.isNotEmpty() }

            domain.getSearchResults(_uiState.value.searchText, selectedItems)
        }
    }

    fun onContinueListeningClick() {
        if (lastPlayedMediaId != null) {
            navigator.navigate(
                Screen.RecitationPlayer(
                    action = "continue",
                    mediaId = lastPlayedMediaId.toString()
                )
            )
        }
    }

    fun onFilterClick() {
        navigator.navigate(Screen.RecitersMenuFilter)
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
            if (allRecitations.first()[reciterId]!!.narrations[narration.id]!!.downloadState
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

    // TODO: find a better fix for this
    fun onSearchTextChange(text: String) {
        searchText = text
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}