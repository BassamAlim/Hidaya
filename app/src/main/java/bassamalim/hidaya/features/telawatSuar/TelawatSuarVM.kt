package bassamalim.hidaya.features.telawatSuar

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.utils.FileUtils
import bassamalim.hidaya.features.destinations.TelawatClientUIDestination
import bassamalim.hidaya.features.destinations.TelawatSuarUIDestination
import bassamalim.hidaya.features.navArgs
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TelawatSuarVM @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val repo: TelawatSuarRepo
): AndroidViewModel(app) {

    private val navArgs = savedStateHandle.navArgs<TelawatSuarNavArgs>()

    private val ver = repo.getVersion(navArgs.reciterId, navArgs.versionId)
    val prefix = "/Telawat/${ver.getReciterId()}/${navArgs.versionId}/"
    private val suraNames = repo.getSuraNames()
    private val searchNames = repo.getSearchNames()
    private val downloading = HashMap<Long, Int>()

    private val _uiState = MutableStateFlow(TelawatSuarState(
        title = repo.getReciterName(navArgs.reciterId),
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

    fun onBackPressed(navigator: DestinationsNavigator) {
        if ((app as Activity).isTaskRoot) {
            navigator.navigate(
                TelawatSuarUIDestination(
                    reciterId = navArgs.reciterId,
                    versionId = navArgs.versionId
                )
            )
            // TODO
//            nc.navigate(Screen.Telawat.route) {
//                popUpTo(Screen.TelawatSuar(reciterId.toString(), versionId.toString()).route) {
//                    inclusive = true
//                }
//            }
        }
        else (app as ComponentActivity).onBackPressedDispatcher.onBackPressed()
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

    fun getItems(page: Int): List<ReciterSura> {
        val listType = ListType.values()[page]

        val items = ArrayList<ReciterSura>()
        val availableSuras = ver.getSuras()
        for (i in 0..113) {
            if (!availableSuras.contains(",${(i + 1)},") ||
                (listType == ListType.Favorite && _uiState.value.favs[i] == 0) ||
                (listType == ListType.Downloaded && !isDownloaded(i))
            ) continue

            items.add(ReciterSura(i, suraNames[i], searchNames[i]))
        }

        return if (_uiState.value.searchText.isEmpty()) items
        else items.filter {
            it.searchName.contains(_uiState.value.searchText, true)
        }
    }

    private fun download(sura: ReciterSura) {
        _uiState.update { it.copy(
            downloadStates = _uiState.value.downloadStates.toMutableList().apply {
                this[sura.num] = DownloadState.Downloading
            }
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

                _uiState.update { it.copy(
                    downloadStates = _uiState.value.downloadStates.toMutableList().apply {
                        this[id] = DownloadState.Downloaded
                    }
                )}

                downloading.remove(downloadId)
            } catch (e: RuntimeException) {
                updateDownloads()
            }
        }
    }

    fun onItemClk(navigator: DestinationsNavigator, sura: ReciterSura) {
        val rId = String.format(Locale.US, "%03d", navArgs.reciterId)
        val vId = String.format(Locale.US, "%02d", navArgs.versionId)
        val sId = String.format(Locale.US, "%03d", sura.num)
        val mediaId = rId + vId + sId

        navigator.navigate(
            TelawatClientUIDestination(
                action = "start",
                mediaId = mediaId
            )
        )
    }

    fun onFavClk(suraNum: Int) {
        val newFav =
            if (_uiState.value.favs[suraNum] == 0) 1
            else 0

        _uiState.update { it.copy(
            favs = _uiState.value.favs.toMutableList().apply {
                this[suraNum] = newFav
            }
        )}

        repo.setFav(suraNum, newFav)

        repo.updateFavorites()
    }

    fun onDownload(sura: ReciterSura) {
        download(sura)
    }

    fun onDelete(suraNum: Int) {
        _uiState.update { it.copy(
            downloadStates = _uiState.value.downloadStates.toMutableList().apply {
                this[suraNum] = DownloadState.NotDownloaded
            }
        )}
    }

    fun onSearchChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}