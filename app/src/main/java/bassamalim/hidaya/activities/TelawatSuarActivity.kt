package bassamalim.hidaya.activities

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.TelawatVersionsDB
import bassamalim.hidaya.enum.DownloadState
import bassamalim.hidaya.enum.ListType
import bassamalim.hidaya.models.ReciterSura
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.FileUtils
import com.google.gson.Gson
import java.io.File
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatSuarActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var pref: SharedPreferences
    private var gson: Gson = Gson()
    private lateinit var ver: TelawatVersionsDB
    private var reciterId = 0
    private var versionId = 0
    private var prefix = ""
    private lateinit var suraNames: List<String>
    private lateinit var searchNames: List<String>
    private var favs = mutableStateListOf<Int>()
    private val downloadStates = mutableStateListOf<DownloadState>()
    private val downloading = HashMap<Long, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        db = DBUtils.getDB(this)
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        reciterId = intent.getIntExtra("reciter_id", 0)
        versionId = intent.getIntExtra("version_id", 0)
        val reciterName = db.telawatRecitersDao().getName(reciterId)

        init()

        setContent {
            AppTheme {
                UI(reciterName)
            }
        }
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

    @Composable
    private fun UI(title: String) {
        MyScaffold(
            title,
            onBack = { onBack() }
        ) {
            val textState = remember { mutableStateOf(TextFieldValue("")) }
            TabLayout(
                pageNames = listOf(getString(R.string.all), getString(R.string.favorite), getString(R.string.downloaded)),
                searchComponent = {
                    SearchComponent(
                        value = textState,
                        hint = stringResource(R.string.search),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            ) { page ->
                Tab(items = getItems(ActivityUtils.getListType(page)), textState)
            }
        }
    }

    @Composable
    private fun Tab(
        items: List<ReciterSura>,
        textState: MutableState<TextFieldValue>
    ) {
        MyLazyColumn(
            lazyList = {
                items(
                    items = items.filter { item ->
                        item.searchName.contains(textState.value.text, ignoreCase = true)
                    }
                ) { item ->
                    SuraCard(sura = item)
                }
            }
        )
    }

    @Composable
    private fun SuraCard(sura: ReciterSura) {
        MyClickableSurface(
            onClick = {
                val intent = Intent(this, TelawatClient::class.java)
                intent.action = "start"
                val rId = String.format(Locale.US, "%03d", reciterId)
                val vId = String.format(Locale.US, "%02d", versionId)
                val sId = String.format(Locale.US, "%03d", sura.num)
                val mediaId = rId + vId + sId
                intent.putExtra("media_id", mediaId)
                startActivity(intent)
            }
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                MyDownloadBtn(
                    state = downloadStates[sura.num],
                    path = "$prefix${sura.num}.mp3",
                    size = 28.dp,
                    deleted = {
                        downloadStates[sura.num] = DownloadState.NotDownloaded
                    }
                ) {
                    download(sura)
                }

                MyText(
                    text = sura.surahName,
                    modifier = Modifier
                        .weight(1F)
                        .padding(10.dp)
                )

                MyFavBtn(favs[sura.num]) {
                    if (favs[sura.num] == 0) {
                        favs[sura.num] = 1
                        db.suarDao().setFav(sura.num, 1)
                    }
                    else if (favs[sura.num] == 1) {
                        favs[sura.num] = 0
                        db.suarDao().setFav(sura.num, 0)
                    }
                    updateFavorites()
                }
            }
        }
    }

}