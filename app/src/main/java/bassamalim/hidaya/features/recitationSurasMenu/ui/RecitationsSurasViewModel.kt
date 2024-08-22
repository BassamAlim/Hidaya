package bassamalim.hidaya.features.recitationSurasMenu.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.FileUtils
import bassamalim.hidaya.features.recitationSurasMenu.domain.RecitationSurasMenuDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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
    private lateinit var suraNames: List<String>
    private val searchNames = domain.getPlainSuraNames()

    private val _uiState = MutableStateFlow(RecitationsSurasUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            language = domain.getLanguage()
            suraNames = domain.getDecoratedSuraNames(language)

            _uiState.update { it.copy(
                title = domain.getReciterName(reciterId, language)
            )}
        }
    }

    fun onStart() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(
                downloadStates = getDownloadStates()
            )}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            app.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        else
            app.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
    }

    fun onStop() {
        try {
            app.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun onBackPressed() {
        val ctx = navigator.getContext()

        if ((ctx as Activity).isTaskRoot) {
            navigator.navigate(Screen.Telawat) {
                popUpTo(Screen.TelawatSuar(reciterId.toString(), narrationId.toString()).route) {
                    inclusive = true
                }
            }
        }
        else (ctx as ComponentActivity).onBackPressedDispatcher.onBackPressed()
    }

    private fun getDownloadStates(): ArrayList<DownloadState> {
        val states = ArrayList<DownloadState>()

        for (i in 0..113) {
            states.add(
                if (isDownloaded(i)) DownloadState.DOWNLOADED
                else DownloadState.NOT_DOWNLOADED
            )
        }

        return states
    }

    fun getItems(page: Int): List<ReciterSura> {
        val listType = ListType.entries[page]

        val favs = domain.getFavs()

        val items = ArrayList<ReciterSura>()
        val availableSuar = narration.availableSuras
        for (i in 0..113) {
            if (!availableSuar.contains(",${(i + 1)},") ||
                (listType == ListType.FAVORITES && favs[i] == 0) ||
                (listType == ListType.DOWNLOADED && !isDownloaded(i))
            ) continue

            items.add(
                ReciterSura(
                    num = i,
                    suraName = suraNames[i],
                    searchName = searchNames[i],
                    isFavorite = mutableIntStateOf(favs[i]),
                )
            )
        }

        return if (_uiState.value.searchText.isEmpty()) items
        else items.filter {
            it.searchName.contains(_uiState.value.searchText, true)
        }
    }

    private fun download(sura: ReciterSura) {
        _uiState.update { it.copy(
            downloadStates = it.downloadStates.toMutableList().apply {
                this[sura.num] = DownloadState.DOWNLOADING
            }
        )}

        val server = narration.url
        val link = String.format(Locale.US, "%s/%03d.mp3", server, sura.num + 1)
        val uri = Uri.parse(link)

        val request = DownloadManager.Request(uri)
        request.setTitle(sura.searchName)
        FileUtils.createDir(app, prefix)
        request.setDestinationInExternalFilesDir(app, prefix, "${sura.num}.mp3")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        val downloadId = (app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
            .enqueue(request)
        downloading[downloadId] = sura.num
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            try {
                val id = downloading[downloadId]!!

                _uiState.update { it.copy(
                    downloadStates = it.downloadStates.toMutableList().apply {
                        this[id] = DownloadState.DOWNLOADED
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

    fun onItemClk(sura: ReciterSura) {
        val rId = String.format(Locale.US, "%03d", reciterId)
        val vId = String.format(Locale.US, "%02d", narrationId)
        val sId = String.format(Locale.US, "%03d", sura.num)
        val mediaId = rId + vId + sId

        navigator.navigate(
            Screen.TelawatClient(
                action = "start",
                mediaId = mediaId
            )
        )
    }

    fun onFavClk(suraNum: Int, newFav: Int) {
        domain.setFav(suraNum, newFav)

        domain.updateFavorites()
    }

    fun onDownload(sura: ReciterSura) {
        Thread {
            download(sura)
        }.start()
    }

    fun onDelete(suraNum: Int) {
        _uiState.update { it.copy(
            downloadStates = it.downloadStates.toMutableList().apply {
                this[suraNum] = DownloadState.NOT_DOWNLOADED
            }
        )}
    }

    fun onSearchChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}