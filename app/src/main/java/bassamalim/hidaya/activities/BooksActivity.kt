package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import bassamalim.hidaya.enum.DownloadState
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.FileUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import java.io.File

class BooksActivity : AppCompatActivity() {

    private lateinit var language: String
    private val prefix = "/Books/"
    private val gson = Gson()
    private lateinit var books: List<BooksDB>
    private val downloadStates = mutableStateListOf<DownloadState>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        language = ActivityUtils.myOnActivityCreated(this)[1]

        books = DBUtils.getDB(this).booksDao().getAll()

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        checkDownloads()
    }

    private fun checkDownloads() {
        downloadStates.clear()
        for (book in books) {
            downloadStates.add(
                if (isDownloaded(book.id)) {
                    if (downloading(book.id)) DownloadState.Downloading
                    else DownloadState.Downloaded
                }
                else DownloadState.NotDownloaded
            )
        }
    }

    private fun isDownloaded(id: Int): Boolean {
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
        downloadStates[item.id] = DownloadState.Downloading

        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference
        // Create a reference with an initial file path and name
        val fileRef = storageRef.child("Books/${item.id}.json")

        FileUtils.createDir(this, prefix)
        val file = File("${getExternalFilesDir(null)}/$prefix/${fileRef.name}")
        file.createNewFile()

        fileRef.getFile(file).addOnSuccessListener {
            Log.i(Global.TAG, "File download succeeded")
            checkDownloads()
        }.addOnFailureListener {
            Log.e(Global.TAG, "File download failed")
        }
    }

    private fun getTitle(item: BooksDB): String {
        return if (language == "en") item.titleEn else item.title
    }

    @Composable
    private fun UI() {
        MyScaffold(
            title = stringResource(R.string.hadeeth_books),
            fab = {
                MyFloatingActionButton(
                    iconId = R.drawable.ic_quran_search,
                    description = stringResource(R.string.search_in_books)
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
                        MyBtnSurface(
                            text = item.title,
                            innerVPadding = 15.dp,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(vertical = 2.dp),
                            iconBtn = {
                                MyDownloadBtn(
                                    state = downloadStates[item.id],
                                    path = "$prefix${item.id}.json",
                                    modifier = Modifier.padding(end = 10.dp),
                                    size = 32.dp,
                                    deleted = {
                                        downloadStates[item.id] = DownloadState.NotDownloaded
                                    }
                                ) {
                                    download(item)
                                }
                            }
                        ) {
                            if (downloadStates[item.id] == DownloadState.NotDownloaded)
                                download(item)
                            else if (downloadStates[item.id] == DownloadState.Downloaded) {
                                val intent = Intent(
                                    this@BooksActivity,
                                    BooksChaptersActivity::class.java
                                )
                                intent.putExtra("book_id", item.id)
                                intent.putExtra("book_title", getTitle(item))
                                startActivity(intent)
                            }
                            else FileUtils.showWaitMassage(this@BooksActivity)
                        }
                    }
                }
            )

            TutorialDialog(
                textResId = R.string.books_activity_tips,
                prefKey = "is_first_time_in_books_activity"
            )
        }
    }
}