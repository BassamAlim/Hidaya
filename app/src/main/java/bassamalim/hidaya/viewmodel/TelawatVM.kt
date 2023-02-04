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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.database.dbs.TelawatDB
import bassamalim.hidaya.enums.DownloadState
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Reciter
import bassamalim.hidaya.repository.TelawatRepo
import bassamalim.hidaya.state.TelawatState
import bassamalim.hidaya.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TelawatVM @Inject constructor(
    private val app: Application,
    private val repository: TelawatRepo
): AndroidViewModel(app) {

    var searchText by mutableStateOf("")
        private set
    val prefix = "/Telawat/"
    private var continueListeningMediaId = ""
    val rewayat = repository.getRewayat()
    private val downloading = HashMap<Long, Pair<Int, Int>>()

    private val _uiState = MutableStateFlow(TelawatState(
        items = getItems(ListType.All),
        favs = repository.getFavs(),
        selectedVersions = repository.getSelectedVersions(),
        continueListeningText = repository.getNoLastPlayStr()
    ))
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(
            isFiltered = _uiState.value.selectedVersions.any { bool -> !bool }
        )}
    }

    fun onStart() {
        setupContinue()

        clean()

        _uiState.update { it.copy(
            downloadStates = getDownloadStates()
        )}

        app.applicationContext.registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    fun onStop() {
        try {
            app.applicationContext.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun onBackPressed(navController: NavController) {
        val ctx = app.applicationContext
        if ((ctx as Activity).isTaskRoot) {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
        else (ctx as ComponentActivity).onBackPressedDispatcher.onBackPressed()
    }

    private fun getDownloadStates(): MutableList<MutableList<DownloadState>> {
        val downloadStates = mutableListOf<MutableList<DownloadState>>()

        for (telawa in repository.getAllVersions()) {  // all versions
            val state =
                if (isDownloaded("${telawa.getReciterId()}/${telawa.getVersionId()}"))
                    DownloadState.Downloaded
                else DownloadState.NotDownloaded

            if (telawa.getReciterId() == downloadStates.size)
                downloadStates.add(mutableListOf(state))
            else
                downloadStates[telawa.getReciterId()].add(state)
        }

        return downloadStates
    }

    private fun setupContinue() {
        continueListeningMediaId = repository.getLastPlayedMediaId()

        if (continueListeningMediaId.isEmpty()) return

        val reciterId = continueListeningMediaId.substring(0, 3).toInt()
        val versionId = continueListeningMediaId.substring(3, 5).toInt()
        val suraIndex = continueListeningMediaId.substring(5).toInt()

        val suraName =
            if (repository.language == Language.ENGLISH) repository.getSuraNamesEn()
            else repository.getSuraNames()
        val reciterName = repository.getReciterName(reciterId)
        val rewaya = repository.getRewaya(reciterId, versionId)

        _uiState.update { it.copy(
            continueListeningText = "${repository.getLastPlayStr()}: " +
                    "${repository.getSuraStr()} ${suraName[suraIndex]} " +
                    "${repository.getForReciterStr()} $reciterName " +
                    "${repository.getInRewayaOfStr()} $rewaya"
        )}
    }

    private fun getItems(type: ListType): List<Reciter> {
        val reciters = repository.getReciters()

        val items = ArrayList<Reciter>()
        for (i in reciters.indices) {
            val reciter = reciters[i]

            if ((type == ListType.Favorite && _uiState.value.favs[i] == 0) ||
                (type == ListType.Downloaded && !isDownloaded("${reciter.id}")))
                continue

            val versions = filterSelectedVersions(repository.getReciterTelawat(reciter.id))
            val versionsList = ArrayList<Reciter.RecitationVersion>()

            versions.forEach { telawa ->
                versionsList.add(
                    Reciter.RecitationVersion(
                        telawa.getVersionId(), telawa.getUrl(), telawa.getRewaya(),
                        telawa.getCount(), telawa.getSuras()
                    )
                )
            }
            items.add(Reciter(reciter.id, reciter.name!!, versionsList))
        }
        return items
    }

    private fun filterSelectedVersions(versions: List<TelawatDB>): List<TelawatDB> {
        if (!_uiState.value.isFiltered) return versions

        val selected = mutableListOf<TelawatDB>()
        for (i in versions.indices) {
            for (j in rewayat.indices) {
                if (_uiState.value.selectedVersions[j]
                    && versions[i].getRewaya().startsWith(rewayat[j])) {
                    selected.add(versions[i])
                    break
                }
            }
        }

        return selected
    }

    private fun isDownloaded(suffix: String): Boolean {
        return File(
            "${app.applicationContext.getExternalFilesDir(null)}$prefix$suffix"
        ).exists()
    }

    private fun downloadVer(reciterId: Int, ver: Reciter.RecitationVersion) {
        val ctx = app.applicationContext

        val downloadStates = _uiState.value.downloadStates.toMutableList()
        downloadStates[reciterId][ver.versionId] = DownloadState.Downloading
        _uiState.update { it.copy(
            downloadStates = downloadStates
        )}

        val downloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var request: DownloadManager.Request
        var posted = false
        for (i in 0..113) {
            if (ver.suras.contains("," + (i + 1) + ",")) {
                val link = String.format(Locale.US, "%s/%03d.mp3", ver.server, i + 1)
                val uri = Uri.parse(link)

                request = DownloadManager.Request(uri)
                request.setTitle("${repository.getReciterName(reciterId)} ${ver.rewaya}")
                val suffix = "$prefix$reciterId/${ver.versionId}"
                FileUtils.createDir(ctx, suffix)
                request.setDestinationInExternalFilesDir(ctx, suffix, "$i.mp3")
                request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )

                val downloadId = downloadManager.enqueue(request)
                if (!posted) {
                    downloading[downloadId] = Pair(reciterId, ver.versionId)
                    posted = true
                }
            }
        }
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            try {
                val ids = downloading[downloadId]!!

                val downloadStates = _uiState.value.downloadStates.toMutableList()
                downloadStates[ids.first][ids.second] = DownloadState.Downloaded
                _uiState.update { it.copy(
                    downloadStates = downloadStates
                )}

                downloading.remove(downloadId)
            } catch (e: RuntimeException) {
                _uiState.update { it.copy(
                    downloadStates = getDownloadStates()
                )}
            }
        }
    }

    private fun clean() {
        val mainDir = File("${app.applicationContext.getExternalFilesDir(null)}/Telawat/")
        FileUtils.deleteDirRecursive(mainDir)
    }

    fun onListTypeChange(pageNum: Int) {
        val listType = ListType.values()[pageNum]

        _uiState.update { it.copy(
            listType = listType,
            items = getItems(listType)
        )}
    }

    fun onContinueListeningClick(navController: NavController) {
        if (continueListeningMediaId.isNotEmpty()) {
            navController.navigate(
                Screen.TelawatClient(
                    "continue",
                    continueListeningMediaId
                ).route
            )
        }
    }

    fun onFilterClick() {
        _uiState.update { it.copy(
            filterDialogShown = true
        )}
    }

    fun onFilterDialogDismiss(booleans: Array<Boolean>) {
        _uiState.update { it.copy(
            filterDialogShown = false,
            selectedVersions = booleans.toList(),
        )}
    }

    fun onFavClick(reciterId: Int) {
        val newFav =
            if (_uiState.value.favs[reciterId] == 0) 1
            else 0

        val favs = _uiState.value.favs.toMutableList()
        favs[reciterId] = newFav
        _uiState.update { it.copy(
            favs = favs
        )}

        repository.setFav(reciterId, newFav)

        repository.updateFavorites()
    }

    fun onDeleted(reciterId: Int, versionId: Int) {
        val downloadStates = _uiState.value.downloadStates.toMutableList()
        downloadStates[reciterId][versionId] = DownloadState.NotDownloaded
        _uiState.update { it.copy(
            downloadStates = downloadStates
        )}
    }

    fun onDownloadClick(reciterId: Int, version: Reciter.RecitationVersion) {
        downloadVer(reciterId, version)
    }

    fun onVersionClick(reciterId: Int, versionId: Int, navController: NavController) {
        navController.navigate(
            Screen.TelawatSuar(
                reciterId.toString(),
                versionId.toString()
            ).route
        )
    }

}