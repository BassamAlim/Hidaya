package bassamalim.hidaya.activities

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityBooksBinding
import bassamalim.hidaya.dialogs.TutorialDialog
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.models.Book.BookInfo
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import java.io.File

class BooksActivity : AppCompatActivity() {

    private var binding: ActivityBooksBinding? = null
    private var gson: Gson? = null
    private var numOfBooks = 0
    private val prefix = "/Books/"
    private var downloaded: BooleanArray? = null
    private var cards: Array<CardView>? = null
    private var downloadBtns: Array<ImageButton>? = null
    private var infoArr: Array<BookInfo?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener {onBackPressed()}
        gson = Gson()
        numOfBooks = resources.getStringArray(R.array.books_titles).size
        downloaded = BooleanArray(numOfBooks)
        checkFirstTime()
        checkDownloaded()
        getInfoArr()
        initViews()
        setListeners()
    }

    private fun checkFirstTime() {
        val key = "is_first_time_in_books_activity"
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(key, true)
        ) TutorialDialog(
            this, getString(R.string.books_activity_tips), key
        )
            .show(this.supportFragmentManager, TutorialDialog.TAG)
    }

    private fun checkDownloaded() {
        val dir = File(getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return
        val files = dir.listFiles()
        for (i in 0 until files!!.size) {
            val file = files[i]
            val name = file.name
            val n = name.substring(0, name.length - 5)
            try {
                val num = n.toInt()
                downloaded!![num] = true
            } catch (ignored: NumberFormatException) {
            }
        }
    }

    private fun getInfoArr() {
        infoArr = arrayOfNulls(numOfBooks)
        val bookTitles: Array<String> = resources.getStringArray(R.array.books_titles)
        val bookAuthors: Array<String> = resources.getStringArray(R.array.books_authors)
        for (i in 0 until numOfBooks) {
            if (downloaded!![i] && !downloading(i)) {
                val path: String = getExternalFilesDir(null).toString() + "/Books/" + i + ".json"
                val jsonStr = Utils.getJsonFromDownloads(path)
                val book: Book = gson!!.fromJson(jsonStr, Book::class.java)
                infoArr!![i] = BookInfo(i, bookTitles[i], bookAuthors[i])
            }
        }
    }

    private fun initViews() {
        cards = arrayOf(binding!!.bokhariCard, binding!!.muslimCard)
        val titleTvs: Array<TextView> = arrayOf(binding!!.bokhariTitleTv, binding!!.muslimTitleTv)
        downloadBtns = arrayOf(binding!!.bokhariDownloadBtn, binding!!.muslimDownloadBtn)

        for (i in 0 until numOfBooks) {
            if (infoArr!![i] != null && downloaded!![i])
                titleTvs[i].text = infoArr!![i]!!.bookTitle
        }

        for (i in 0 until numOfBooks) updateUI(i, downloaded!![i])
    }

    private fun setListeners() {
        for (i in 0 until numOfBooks) {
            cards!![i].setOnClickListener {
                if (downloaded(i)) {
                    if (downloading(i)) Toast.makeText(
                        this, getString(R.string.wait_for_download),
                        Toast.LENGTH_SHORT
                    ).show() else {
                        val intent = Intent(
                            this,
                            BooksChaptersCollectionActivity::class.java
                        )
                        intent.putExtra("book_id", i)
                        intent.putExtra(
                            "book_title",
                            resources.getStringArray(R.array.books_titles)[i]
                        )
                        startActivity(intent)
                    }
                } else {
                    getLinkAndRequestDownload(i)
                    updateUI(i, true)
                }
            }
            downloadBtns!![i].setOnClickListener {
                if (downloaded(i)) {
                    if (downloading(i)) Toast.makeText(
                        this, getString(R.string.wait_for_download), Toast.LENGTH_SHORT
                    ).show() else {
                        Utils.deleteFile(this, "$prefix$i.json")
                        updateUI(i, false)
                    }
                } else {
                    getLinkAndRequestDownload(i)
                    updateUI(i, true)
                }
            }
        }
        binding!!.fab.setOnClickListener {
            val intent = Intent(this, BookSearcher::class.java)
            startActivity(intent)
        }
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
        Log.d(Global.TAG, "here")
        val path: String = getExternalFilesDir(null).toString() + "/Books/" + id + ".json"
        val jsonStr = Utils.getJsonFromDownloads(path)
        return try {
            gson!!.fromJson(jsonStr, Book::class.java)
            false
        } catch (e: Exception) {
            true
        }
    }

    private fun updateUI(id: Int, downloaded: Boolean) {
        if (downloaded) downloadBtns!![id].setImageDrawable(
            AppCompatResources.getDrawable(
                this, R.drawable.ic_downloaded
            )
        ) else downloadBtns!![id].setImageDrawable(
            AppCompatResources.getDrawable(
                this, R.drawable.ic_download
            )
        )
    }

    private fun getLinkAndRequestDownload(id: Int) {
        val links = arrayOfNulls<String>(2)
        val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                links[0] = remoteConfig.getString("sahih_albokhari_url")
                links[1] = remoteConfig.getString("sahih_muslim_url")
                Log.d(Global.TAG, "Config params updated")
                Log.d(Global.TAG, "Sahih Albokhari URL: " + links[0])
                Log.d(Global.TAG, "Sahih Muslim URL: " + links[1])
                download(id, links[id])
            } else Log.d(Global.TAG, "Fetch failed")
        }
    }

    private fun download(id: Int, link: String?) {
        Log.d(Global.TAG, link!!)
        val downloadManager: DownloadManager =
            getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(link)
        val request: DownloadManager.Request = DownloadManager.Request(uri)
        val title: String = String.format(
            getString(R.string.book_downloading),
            resources.getStringArray(R.array.books_titles)[id]
        )
        request.setTitle(title)
        request.setVisibleInDownloadsUi(true)
        Utils.createDir(this, prefix)
        request.setDestinationInExternalFilesDir(this, prefix, "$id.json")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        downloadManager.enqueue(request)
    }
}