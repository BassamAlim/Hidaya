package bassamalim.hidaya.viewmodel

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.enums.DownloadState
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.ReciterSura
import bassamalim.hidaya.repository.TelawatSuarRepo
import bassamalim.hidaya.state.TelawatSuarState
import bassamalim.hidaya.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TelawatSuarVM @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val repo: TelawatSuarRepo
): AndroidViewModel(app) {

    private val reciterId = savedStateHandle.get<Int>("reciter_id") ?: 0
    private val versionId = savedStateHandle.get<Int>("version_id") ?: 0

    private val ver = repo.getVersion(reciterId, versionId)
    val prefix = "/Telawat/${ver.getReciterId()}/${versionId}/"
    private val suraNames = repo.getSuraNames()
    private val searchNames = repo.getSearchNames()
    private val downloading = HashMap<Long, Int>()
    var searchText by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(TelawatSuarState(
        title = repo.getReciterName(reciterId),
        items = getItems(ListType.All),
        favs = repo.getFavs()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        onStart()
    }

    fun onStart() {
        updateDownloads()

        app.registerReceiver(
            onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    fun onStop() {
        try {
            app.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun onBackPressed(nc: NavController) {
        if ((nc.context as Activity).isTaskRoot) {
            nc.navigate(Screen.Telawat.route) {
                popUpTo(Screen.Telawat.route) { inclusive = true }
            }
        }
        else (app.applicationContext as ComponentActivity).onBackPressedDispatcher.onBackPressed()
    }

    private fun updateDownloads() {
        val states = ArrayList<DownloadState>()
        for (i in 0..113) {
            states.add(
                if (isDownloaded(i)) DownloadState.Downloaded
                else DownloadState.NotDownloaded
            )
        }

        _uiState.update { it.copy(
            downloadStates = states
        )}
    }

    private fun isDownloaded(suraNum: Int): Boolean {
        return File(
            "${app.getExternalFilesDir(null)}$prefix$suraNum.mp3"
        ).exists()
    }

    private fun getItems(type: ListType): List<ReciterSura> {
        val items = ArrayList<ReciterSura>()
        val availableSuras = ver.getSuras()
        for (i in 0..113) {
            if (!availableSuras.contains(",${(i + 1)},") ||
                (type == ListType.Favorite && _uiState.value.favs[i] == 0) ||
                (type == ListType.Downloaded && !isDownloaded(i))
            ) continue

            items.add(ReciterSura(i, suraNames[i], searchNames[i]))
        }

        return if (searchText.isEmpty()) items
        else items.filter { it.searchName.contains(searchText, true) }
    }

    private fun download(sura: ReciterSura) {
        val states = _uiState.value.downloadStates.toMutableList()
        states[sura.num] = DownloadState.Downloading
        _uiState.update { it.copy(
            downloadStates = states
        )}

        val server = ver.getUrl()
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

                val states = _uiState.value.downloadStates.toMutableList()
                states[id] = DownloadState.Downloaded
                _uiState.update { it.copy(
                    downloadStates = states
                )}

                downloading.remove(downloadId)
            } catch (e: RuntimeException) {
                updateDownloads()
            }
        }
    }

    fun onListTypeChange(page: Int, currentPage: Int) {
        if (page != currentPage) return

        _uiState.update { it.copy(
            items = getItems(ListType.values()[page])
        )}
    }

    fun onItemClk(navController: NavController, sura: ReciterSura) {
        val rId = String.format(Locale.US, "%03d", reciterId)
        val vId = String.format(Locale.US, "%02d", versionId)
        val sId = String.format(Locale.US, "%03d", sura.num)
        val mediaId = rId + vId + sId

        navController.navigate(
            Screen.TelawatClient(
                "start",
                mediaId
            ).route
        )
    }

    fun onFavClk(suraNum: Int) {
        val newFav =
            if (_uiState.value.favs[suraNum] == 0) 1
            else 0

        val favs = _uiState.value.favs.toMutableList()
        favs[reciterId] = newFav
        _uiState.update { it.copy(
            favs = favs
        )}

        repo.setFav(suraNum, newFav)

        repo.updateFavorites()
    }

    fun onDownload(sura: ReciterSura) {
        download(sura)
    }

    fun onDelete(suraNum: Int) {
        val states = _uiState.value.downloadStates.toMutableList()
        states[suraNum] = DownloadState.NotDownloaded
        _uiState.update { it.copy(
            downloadStates = states
        )}
    }

    fun onSearchChange(text: String) {
        searchText = text

        _uiState.update { it.copy(
            items = getItems(ListType.All)
        )}
    }

}