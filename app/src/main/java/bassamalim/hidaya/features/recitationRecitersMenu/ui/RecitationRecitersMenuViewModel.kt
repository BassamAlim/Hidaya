package bassamalim.hidaya.features.recitationRecitersMenu.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.data.database.models.Recitation
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.Reciter
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.recitationRecitersMenu.domain.RecitationRecitersMenuDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val narrations = domain.getAllNarrations()
    private lateinit var suraNames: List<String>

    private val _uiState = MutableStateFlow(RecitationRecitersMenuUiState(
        narrationSelections = domain.getSelectedNarrations(),
        continueListeningText = domain.getNoLastPlayStr()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            language = domain.getLanguage()

            suraNames = domain.getSuraNames(language)
        }

        _uiState.update { it.copy(
            isFiltered = it.narrationSelections.values.any { bool -> !bool }
        )}
    }

    fun onStart() {
        setupContinue()

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
                popUpTo(Screen.Telawat.route) {
                    inclusive = true
                }
            }
        }
        else (context as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
    }

    private fun getDownloadStates(): Map<Int, DownloadState> {
        val downloadStates = mutableMapOf<Int, DownloadState>()

        for (recitation in domain.getAllNarrations()) {
            downloadStates[recitation.narration_id] =
                if (domain.checkIsDownloaded(recitation.reciter_id, recitation.narration_id))
                    DownloadState.DOWNLOADED
                else
                    DownloadState.NOT_DOWNLOADED
        }

        return downloadStates
    }

    private fun setupContinue() {
        continueListeningMediaId = domain.getLastPlayedMediaId()

        if (continueListeningMediaId.isEmpty() || continueListeningMediaId == "00000000") return  // added the second part to prevent errors due to change in db
        Log.d("TelawatVM", "continueListeningMediaId: $continueListeningMediaId")

        val reciterId = continueListeningMediaId.substring(0, 3).toInt()
        val narrationId = continueListeningMediaId.substring(3, 5).toInt()
        val suraIndex = continueListeningMediaId.substring(5).toInt()
        Log.d("TelawatVM", "reciterId: $reciterId, narrationId: $narrationId, suraIndex: $suraIndex")

        val reciterName = domain.getReciterName(reciterId, language)
        val rewaya = domain.getNarration(reciterId, narrationId)

        _uiState.update { it.copy(
            continueListeningText = "${domain.getLastPlayStr()}: " +
                    "${domain.getSuraStr()} ${suraNames[suraIndex]} " +
                    "${domain.getForReciterStr()} $reciterName " +
                    "${domain.getInRewayaOfStr()} $rewaya"
        )}
    }

    fun getItems(page: Int): List<Reciter> {
        val listType = ListType.entries[page]

        val reciters = domain.getReciters()

        val favs = domain.getFavs()

        val items = ArrayList<Reciter>()
        for (i in reciters.indices) {
            val reciter = reciters[i]

            if ((listType == ListType.FAVORITES && favs[i] == 0) ||
                (listType == ListType.DOWNLOADED && !isDownloaded("${reciter.id}")))
                continue

            val narrations = filterSelectedNarrations(domain.getReciterRecitations(reciter.id))
            val narrationsList = ArrayList<Reciter.RecitationNarration>()

            narrations.forEach { telawa ->
                narrationsList.add(
                    Reciter.RecitationNarration(
                        id = telawa.narration_id,
                        server = telawa.narration_url,
                        name =
                            if (domain.language == Language.ARABIC) telawa.narration_name_ar
                            else telawa.narration_name_en,
                        count = telawa.rewayah_surah_total,
                        availableSuras = telawa.narration_available_suras
                    )
                )
            }

            items.add(
                Reciter(
                    id = reciter.id,
                    name =
                        if (language == Language.ARABIC) reciter.nameAr
                        else reciter.nameEn,
                    narrations = narrationsList,
                    isFavorite = favs[i]
                )
            )
        }

        return if (_uiState.value.searchText.isEmpty()) items
        else items.filter { reciter ->
            reciter.name.contains(_uiState.value.searchText, true)
        }
    }

    private fun filterSelectedNarrations(narrations: List<Recitation>): List<Recitation> {
        if (!_uiState.value.isFiltered) return narrations

        val selected = mutableListOf<Recitation>()
        for (i in narrations.indices) {
            for (j in this.narrations.indices) {
                if (_uiState.value.narrationSelections[j]!!
                    && narrations[i].narration_name_ar
                        .startsWith(this.narrations[j].narration_name_ar)) {
                    selected.add(narrations[i])
                    break
                }
            }
        }

        return selected
    }

    private fun downloadNarration(reciterId: Int, ver: Reciter.RecitationNarration) {

    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            try {
                val narrationId = downloading[downloadId]!!
                _uiState.update { it.copy(
                    downloadStates = it.downloadStates.toMutableMap().apply {
                        this[narrationId] = DownloadState.DOWNLOADED
                    }
                )}

                downloading.remove(downloadId)
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
                Screen.TelawatClient(
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

    fun onDownloadNarrationClick(reciterId: Int, narration: Reciter.RecitationNarration) {
        if (_uiState.value.downloadStates[reciterId] == DownloadState.NOT_DOWNLOADED) {
            _uiState.update { it.copy(
                downloadStates = it.downloadStates.toMutableMap().apply {
                    this[reciterId] = DownloadState.DOWNLOADING
                }
            )}

            domain.downloadNarration(reciterId, narration)
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
            Screen.TelawatSuar(
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