package bassamalim.hidaya.features.recitations.recitationSurasMenu.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.recitations.recitationSurasMenu.domain.RecitationSurasMenuDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RecitationsSurasViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: RecitationSurasMenuDomain,
    private val navigator: Navigator
): ViewModel() {

    private val reciterId = savedStateHandle.get<Int>("reciter_id") ?: 0
    private val narrationId = savedStateHandle.get<Int>("narration_id") ?: 0

    private lateinit var language: Language
    private val narration = domain.getNarration(reciterId, narrationId)
    private lateinit var decoratedSuraNames: List<String>
    private val plainSuraNames = domain.getPlainSuraNames()
    private val suraFavorites = domain.getSuraFavorites()

    private val _uiState = MutableStateFlow(RecitationsSurasUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            language = domain.getLanguage()
            decoratedSuraNames = domain.getDecoratedSuraNames(language)

            _uiState.update { it.copy(
                title = domain.getReciterName(reciterId, language)
            )}
        }
    }

    fun onStart() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(
                downloadStates = domain.getDownloadStates()
            )}
        }

        domain.registerDownloadReceiver(onComplete)
    }

    fun onStop() {
        domain.unregisterDownloadReceiver(onComplete)
    }

    fun onBackPressed() {
        val ctx = navigator.getContext()

        if ((ctx as Activity).isTaskRoot) {
            navigator.navigate(Screen.RecitationsRecitersMenu) {
                popUpTo(
                    Screen.RecitationSurasMenu(
                        reciterId = reciterId.toString(),
                        narrationId = narrationId.toString()
                    ).route
                ) {
                    inclusive = true
                }
            }
        }
        else (ctx as ComponentActivity).onBackPressedDispatcher.onBackPressed()
    }

    fun getItems(page: Int): Flow<List<ReciterSura>> {
        val listType = ListType.entries[page]
        val availableSuar = narration.availableSuras

        return suraFavorites.map { favoriteSuras ->
            (0..113).filter { i ->
                (availableSuar.contains(",${(i + 1)},") ||
                        (listType == ListType.FAVORITES && favoriteSuras[i]!!) ||
                        (listType == ListType.DOWNLOADED && domain.checkIsDownloaded(i))) &&
                        (_uiState.value.searchText.isEmpty() ||
                                plainSuraNames[i].contains(_uiState.value.searchText, true))
            }.map { i ->
                ReciterSura(
                    id = i,
                    suraName = decoratedSuraNames[i],
                    searchName = plainSuraNames[i],
                    isFavorite = favoriteSuras[i]!!
                )
            }
        }
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            try {
                val suraId = domain.getDownloadingSuraId(downloadId)

                _uiState.update { it.copy(
                    downloadStates = it.downloadStates.toMutableMap().apply {
                        this[suraId] = DownloadState.DOWNLOADED
                    }
                )}

                domain.removeFromDownloading(downloadId)
            } catch (e: RuntimeException) {
                _uiState.update { it.copy(
                    downloadStates = domain.getDownloadStates()
                )}
            }
        }
    }

    fun onSuraClick(suraId: Int) {
        val formattedReciterId = String.format(Locale.US, "%03d", reciterId)
        val formattedNarrationId = String.format(Locale.US, "%02d", narrationId)
        val formattedSuraId = String.format(Locale.US, "%03d", suraId)
        val mediaId = formattedReciterId + formattedNarrationId + formattedSuraId

        navigator.navigate(
            Screen.RecitationPlayer(action = "start", mediaId = mediaId)
        )
    }

    fun onFavoriteClick(sura: ReciterSura) {
        viewModelScope.launch {
            domain.setFavoriteStatus(suraId = sura.id, value = !sura.isFavorite)
        }
    }

    fun onDownloadClick(sura: ReciterSura) {
        if (_uiState.value.downloadStates[sura.id] == DownloadState.NOT_DOWNLOADED) {
            _uiState.update { it.copy(
                downloadStates = it.downloadStates.toMutableMap().apply {
                    this[sura.id] = DownloadState.DOWNLOADING
                }
            )}

            domain.download(sura = sura, server = narration.url)
        }
        else {
            _uiState.update { it.copy(
                downloadStates = it.downloadStates.toMutableMap().apply {
                    this[sura.id] = DownloadState.NOT_DOWNLOADED
                }
            )}

            domain.delete(reciterId = reciterId, narrationId = narrationId, suraId = sura.id)
        }
    }

    fun onSearchChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}