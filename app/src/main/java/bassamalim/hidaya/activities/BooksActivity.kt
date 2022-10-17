package bassamalim.hidaya.activities

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.database.dbs.BooksDB
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.FileUtils
import com.google.gson.Gson
import java.io.File

class BooksActivity : AppCompatActivity() {

    private lateinit var language: String
    private val prefix = "/Books/"
    private val gson = Gson()
    private val downloadingIds = HashMap<Long, Int>()
    private val downloadStates = mutableStateListOf<String>()
    private lateinit var books: List<BooksDB>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = ActivityUtils.onActivityCreateSetLocale(this)

        books = DBUtils.getDB(this).booksDao().getAll()

        initStates()

        ActivityUtils.checkFirstTime(
            this, supportFragmentManager, "is_first_time_in_books_activity",
            R.string.books_activity_tips
        )

        setContent {
            AppTheme {
                UI()
            }
        }

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun initStates() {
        for (book in books) downloadStates.add("not downloaded")
    }

    private fun showWaitMassage() {
        Toast.makeText(
            this, getString(R.string.wait_for_download), Toast.LENGTH_SHORT
        ).show()
    }

    private fun downloaded(id: Int): Boolean {
        val dir = File(getExternalFilesDir(null).toString() + prefix)

        if (!dir.exists()) return false

        val files = dir.listFiles()
        for (i in 0 until files!!.size) {
            val name = files[i].name
            val n = name.substring(0, name.length - 5)
            try {
                val num = n.toInt()
                if (num == id) return true
            } catch (ignored: NumberFormatException) {}
        }
        return false
    }

    private fun downloading(id: Int): Boolean {
        val path = getExternalFilesDir(null).toString() + "/Books/" + id + ".json"

        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return try {
            gson.fromJson(jsonStr, Book::class.java)
            false
        } catch (e: Exception) {
            true
        }
    }

    private fun download(item: BooksDB) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(item.url)
        val request = DownloadManager.Request(uri)
        request.setTitle(getTitle(item))
        FileUtils.createDir(this, prefix)
        request.setDestinationInExternalFilesDir(this, prefix, "${item.id}.json")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        val downloadId = downloadManager.enqueue(request)

        downloadingIds[downloadId] = item.id
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            try {
                val id = downloadingIds[downloadId]!!
                downloadStates[id] = "downloaded"
                downloadingIds.remove(downloadId)
            } catch (e: RuntimeException) {
                for (i in downloadStates.indices) downloadStates[i] = downloadStates[i]
            }
        }
    }

    private fun getTitle(item: BooksDB): String {
        return if (language == "en") item.titleEn else item.title
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }

    @Composable
    private fun UI() {
        MyScaffold(
            title = stringResource(id = R.string.hadeeth_books),
            fab = {
                  MyFloatingActionButton(
                      iconId = R.drawable.ic_quran_search,
                      description = stringResource(id = R.string.search_in_books)
                  ) {
                      startActivity(Intent(this, BookSearcher::class.java))
                  }
            }
        ) {
            MyLazyColumn(
                Modifier.padding(vertical = 5.dp),
                lazyList = {
                    items(
                        items = books
                    ) { item ->
                        downloadStates[item.id] =
                            if (downloaded(item.id)) {
                                if (downloading(item.id)) "downloading"
                                else "downloaded"
                            }
                            else "not downloading"

                        MyBtnSurface(
                            text = item.title,
                            innerVPadding = 15.dp,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(vertical = 2.dp),
                            iconBtn = {
                                if (downloadStates[item.id] == "downloading")
                                    MyCircularProgressIndicator()
                                else {
                                    MyIconBtn(
                                        iconId =
                                        if (downloadStates[item.id] == "downloaded")
                                            R.drawable.ic_downloaded
                                        else
                                            R.drawable.ic_download,
                                        description = stringResource(id = R.string.download_description),
                                        tint = AppTheme.colors.accent
                                    ) {
                                        if (downloadingIds.containsValue(item.id)) showWaitMassage()
                                        else if (downloaded(item.id)) {
                                            if (downloading(item.id)) showWaitMassage()
                                            else {
                                                FileUtils.deleteFile(
                                                    this@BooksActivity,
                                                    "$prefix${item.id}.json"
                                                )
                                                downloadStates[item.id] = "not downloaded"
                                            }
                                        }
                                        else {
                                            download(item)
                                            downloadStates[item.id] = "downloading"
                                        }
                                    }
                                }
                            }
                        ) {
                            if (downloadingIds.containsValue(item.id)) showWaitMassage()
                            else if (downloaded(item.id)) {
                                if (downloading(item.id)) showWaitMassage()
                                else {
                                    val intent = Intent(
                                        this@BooksActivity,
                                        BooksChaptersActivity::class.java
                                    )
                                    intent.putExtra("book_id", item.id)
                                    intent.putExtra("book_title", getTitle(item))
                                    startActivity(intent)
                                }
                            }
                            else {
                                download(item)
                                downloadStates[item.id] = "downloading"
                            }
                        }
                    }
                }
            )
        }
    }
}