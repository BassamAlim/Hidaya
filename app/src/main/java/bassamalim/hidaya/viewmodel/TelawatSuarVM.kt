package bassamalim.hidaya.viewmodel

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.database.dbs.TelawatVersionsDB
import bassamalim.hidaya.enum.DownloadState
import bassamalim.hidaya.enum.ListType
import bassamalim.hidaya.models.ReciterSura
import bassamalim.hidaya.repository.TelawatSuarRepo
import bassamalim.hidaya.state.TelawatSuarState
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.SearchComponent
import bassamalim.hidaya.ui.components.TabLayout
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TelawatSuarVM @Inject constructor(
    private val repository: TelawatSuarRepo
): ViewModel() {

    private lateinit var ver: TelawatVersionsDB
    private var reciterId = 0
    private var versionId = 0
    private var prefix = ""
    private lateinit var suraNames: List<String>
    private lateinit var searchNames: List<String>
    private var favs = mutableStateListOf<Int>()
    private val downloadStates = mutableStateListOf<DownloadState>()
    private val downloading = HashMap<Long, Int>()

    private val _uiState = MutableStateFlow(TelawatSuarState())
    val uiState = _uiState.asStateFlow()

    init {
        reciterId = intent.getIntExtra("reciter_id", 0)
        versionId = intent.getIntExtra("version_id", 0)
        val reciterName = db.telawatRecitersDao().getName(reciterId)

        init()
    }

    override fun onPause() {
        super.onPause()

        try {
            unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()

        checkDownloads()

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun onBack() {
        if (isTaskRoot) {
            startActivity(Intent(this, TelawatActivity::class.java))
            finish()
        }
        else onBackPressedDispatcher.onBackPressed()
    }

    override fun onBackPressed() {
        onBack()
    }

    private fun init() {
        ver = db.telawatVersionsDao().getVersion(reciterId, versionId)
        suraNames = db.suarDao().getNames()
        searchNames = db.suarDao().getSearchNames()

        prefix = "/Telawat/${ver.getReciterId()}/${versionId}/"

        setupFavs()
    }

    private fun setupFavs() {
        for (fav in db.suarDao().getFavs()) favs.add(fav)
    }

    private fun checkDownloads() {
        downloadStates.clear()
        for (i in 0..113) {
            downloadStates.add(
                if (isDownloaded(i)) DownloadState.Downloaded
                else DownloadState.NotDownloaded
            )
        }
    }

    private fun isDownloaded(suraNum: Int): Boolean {
        return File("${getExternalFilesDir(null)}$prefix$suraNum.mp3").exists()
    }

    private fun getItems(type: ListType): List<ReciterSura> {
        val items = ArrayList<ReciterSura>()
        val availableSuras = ver.getSuras()
        for (i in 0..113) {
            if (!availableSuras.contains(",${(i + 1)},") ||
                (type == ListType.Favorite && favs[i] == 0) ||
                (type == ListType.Downloaded && !isDownloaded(i))
            ) continue

            items.add(ReciterSura(i, suraNames[i], searchNames[i]))
        }
        return items
    }

    private fun download(sura: ReciterSura) {
        downloadStates[sura.num] = DownloadState.Downloading

        val server = ver.getUrl()
        val link = String.format(Locale.US, "%s/%03d.mp3", server, sura.num + 1)
        val uri = Uri.parse(link)

        val request = DownloadManager.Request(uri)
        request.setTitle(sura.searchName)
        FileUtils.createDir(this, prefix)
        request.setDestinationInExternalFilesDir(this, prefix, "${sura.num}.mp3")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        val downloadId = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
            .enqueue(request)
        downloading[downloadId] = sura.num
    }

    private fun updateFavorites() {
        val favSuras = db.suarDao().getFavs().toTypedArray()
        val surasJson = gson.toJson(favSuras)
        pref.edit()
            .putString("favorite_suras", surasJson)
            .apply()
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            try {
                val id = downloading[downloadId]!!
                downloadStates[id] = DownloadState.Downloaded
                downloading.remove(downloadId)
            } catch (e: RuntimeException) {
                checkDownloads()
            }
        }
    }

}