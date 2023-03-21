package bassamalim.hidaya.features.telawat

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.data.database.dbs.TelawatDB
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.Reciter
import bassamalim.hidaya.core.utils.FileUtils
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
    private val repo: TelawatRepo
): AndroidViewModel(app) {

    private var listType = ListType.All
    var searchText by mutableStateOf("")
        private set
    val prefix = "/Telawat/"
    private var continueListeningMediaId = ""
    val rewayat = repo.getRewayat()
    private val downloading = HashMap<Long, Pair<Int, Int>>()
    private val suraNames =
        if (repo.language == Language.ENGLISH) repo.getSuraNamesEn()
        else repo.getSuraNames()

    private val _uiState = MutableStateFlow(
        TelawatState(
        favs = repo.getFavs(),
        selectedVersions = repo.getSelectedVersions(),
        continueListeningText = repo.getNoLastPlayStr()
    )
    )
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
            items = getItems(listType),
            downloadStates = getDownloadStates()
        )}

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

    fun onBackPressed(navController: NavController) {
        val ctx = navController.context

        if ((ctx as Activity).isTaskRoot) {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Telawat.route) {
                    inclusive = true
                }
            }
        }
        else (ctx as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
    }

    private fun getDownloadStates(): List<List<DownloadState>> {
        val downloadStates = mutableListOf<MutableList<DownloadState>>()

        for (telawa in repo.getAllVersions()) {  // all versions
            val state =
                if (isDownloaded("${telawa.getReciterId()}/${telawa.getVersionId()}"))
                    DownloadState.Downloaded
                else
                    DownloadState.NotDownloaded

            if (telawa.getReciterId() == downloadStates.size)
                downloadStates.add(mutableListOf(state))
            else
                downloadStates[telawa.getReciterId()].add(state)
        }

        return downloadStates
    }

    private fun setupContinue() {
        continueListeningMediaId = repo.getLastPlayedMediaId()

        if (continueListeningMediaId.isEmpty()) return

        val reciterId = continueListeningMediaId.substring(0, 3).toInt()
        val versionId = continueListeningMediaId.substring(3, 5).toInt()
        val suraIndex = continueListeningMediaId.substring(5).toInt()

        val reciterName = repo.getReciterName(reciterId)
        val rewaya = repo.getRewaya(reciterId, versionId)

        _uiState.update { it.copy(
            continueListeningText = "${repo.getLastPlayStr()}: " +
                    "${repo.getSuraStr()} ${suraNames[suraIndex]} " +
                    "${repo.getForReciterStr()} $reciterName " +
                    "${repo.getInRewayaOfStr()} $rewaya"
        )}
    }

    private fun getItems(type: ListType): List<Reciter> {
        val reciters = repo.getReciters()

        val items = ArrayList<Reciter>()
        for (i in reciters.indices) {
            val reciter = reciters[i]

            if ((type == ListType.Favorite && _uiState.value.favs[i] == 0) ||
                (type == ListType.Downloaded && !isDownloaded("${reciter.id}")))
                continue

            val versions = filterSelectedVersions(repo.getReciterTelawat(reciter.id))
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
            "${app.getExternalFilesDir(null)}$prefix$suffix"
        ).exists()
    }

    private fun downloadVer(reciterId: Int, ver: Reciter.RecitationVersion) {
        val downloadManager = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var request: DownloadManager.Request
        var posted = false
        val suraStr = repo.getSuraStr()
        for (i in 0..113) {
            if (ver.suras.contains("," + (i + 1) + ",")) {
                val link = String.format(Locale.US, "%s/%03d.mp3", ver.server, i + 1)
                val uri = Uri.parse(link)

                request = DownloadManager.Request(uri)
                request.setTitle(
                    "${repo.getReciterName(reciterId)} ${ver.rewaya} $suraStr ${suraNames[i]}"
                )
                val suffix = "$prefix$reciterId/${ver.versionId}"
                FileUtils.createDir(app, suffix)
                request.setDestinationInExternalFilesDir(app, suffix, "$i.mp3")
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

                _uiState.update { it.copy(
                    downloadStates = _uiState.value.downloadStates.toMutableList().apply {
                        val innerStates = this[ids.first].toMutableList()
                        innerStates[ids.second] = DownloadState.Downloaded
                        this[ids.first] = innerStates
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

    private fun clean() {
        val mainDir = File("${app.getExternalFilesDir(null)}/Telawat/")
        FileUtils.deleteDirRecursive(mainDir)
    }

    fun onPageChg(page: Int, currentPage: Int) {
        if (page != currentPage) return

        listType = ListType.values()[page]

        _uiState.update { it.copy(
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

    fun onFilterClk() {
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

    fun onFavClk(reciterId: Int) {
        val newFav =
            if (_uiState.value.favs[reciterId] == 0) 1
            else 0

        val favs = _uiState.value.favs.toMutableList()
        favs[reciterId] = newFav
        _uiState.update { it.copy(
            favs = favs
        )}

        repo.setFav(reciterId, newFav)

        repo.updateFavorites()
    }

    fun onDeleted(reciterId: Int, versionId: Int) {
        _uiState.update { it.copy(
            downloadStates = _uiState.value.downloadStates.toMutableList().apply {
                val innerStates = this[reciterId].toMutableList()
                innerStates[versionId] = DownloadState.NotDownloaded
                this[reciterId] = innerStates
            }
        )}
    }

    fun onDownloadClk(reciterId: Int, version: Reciter.RecitationVersion) {
        _uiState.update { it.copy(
            downloadStates = _uiState.value.downloadStates.toMutableList().apply {
                val innerStates = this[reciterId].toMutableList()
                innerStates[version.versionId] = DownloadState.Downloading
                this[reciterId] = innerStates
            }
        )}

        downloadVer(reciterId, version)
    }

    fun onVersionClk(reciterId: Int, versionId: Int, navController: NavController) {
        navController.navigate(
            Screen.TelawatSuar(
                reciterId.toString(),
                versionId.toString()
            ).route
        )
    }

    fun onSearchTextCh(text: String) {
        searchText = text

        _uiState.update { it.copy(
            items = getItems(listType)
        )}
    }

}