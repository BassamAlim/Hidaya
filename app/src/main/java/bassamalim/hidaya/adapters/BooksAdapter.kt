package bassamalim.hidaya.adapters

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.BooksChaptersCollectionActivity
import bassamalim.hidaya.database.dbs.BooksDB
import bassamalim.hidaya.models.Book
import bassamalim.hidaya.utils.FileUtils
import com.google.gson.Gson
import java.io.File

class BooksAdapter(
    private val context: Context, private val items: MutableList<BooksDB>,
    private val language: String
    ) : RecyclerView.Adapter<BooksAdapter.ViewHolder?>() {

    private val prefix = "/Books/"
    private val gson = Gson()
    private val downloadingIds = HashMap<Long, Int>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView
        val titleTv: TextView
        val downloadBtn: ImageButton
        val downloadingCircle: ProgressBar

        init {
            card = view.findViewById(R.id.card)
            titleTv = view.findViewById(R.id.title_tv)
            downloadBtn = view.findViewById(R.id.download_btn)
            downloadingCircle = view.findViewById(R.id.buffering_circle)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(
                R.layout.item_book, viewGroup, false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = items[position]

        viewHolder.titleTv.text = getTitle(item)

        setListeners(viewHolder, item)

        if (downloaded(item.id)) {
            if (downloading(item.id)) updateUI(viewHolder, "downloading")
            else updateUI(viewHolder, "downloaded")
        }
        else updateUI(viewHolder, "not downloading")
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun setListeners(viewHolder: ViewHolder, item: BooksDB) {
        viewHolder.card.setOnClickListener {
            if (downloadingIds.containsValue(item.id)) showWaitMassage()
            else if (downloaded(item.id)) {
                if (downloading(item.id)) showWaitMassage()
                else {
                    val intent = Intent(context, BooksChaptersCollectionActivity::class.java)
                    intent.putExtra("book_id", item.id)
                    intent.putExtra("book_title", getTitle(item))
                    context.startActivity(intent)
                }
            }
            else {
                download(item)
                updateUI(viewHolder, "downloading")
            }
        }

        viewHolder.downloadBtn.setOnClickListener {
            if (downloadingIds.containsValue(item.id)) showWaitMassage()
            else if (downloaded(item.id)) {
                if (downloading(item.id)) showWaitMassage()
                else {
                    FileUtils.deleteFile(context, "$prefix${item.id}.json")
                    updateUI(viewHolder, "not downloaded")
                }
            }
            else {
                download(item)
                updateUI(viewHolder, "downloading")
            }
        }
    }

    private fun showWaitMassage() {
        Toast.makeText(
            context, context.getString(R.string.wait_for_download), Toast.LENGTH_SHORT
        ).show()
    }

    private fun downloaded(id: Int): Boolean {
        val dir = File(context.getExternalFilesDir(null).toString() + prefix)

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
        val path = context.getExternalFilesDir(null).toString() + "/Books/" + id + ".json"

        val jsonStr = FileUtils.getJsonFromDownloads(path)
        return try {
            gson.fromJson(jsonStr, Book::class.java)
            false
        } catch (e: Exception) {
            true
        }
    }

    private fun updateUI(viewHolder: ViewHolder, status: String) {
        when(status) {
            "downloaded" -> {
                viewHolder.downloadingCircle.visibility = View.GONE
                viewHolder.downloadBtn.visibility = View.VISIBLE
                viewHolder.downloadBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_downloaded)
                )
            }
            "not downloaded" -> {
                viewHolder.downloadingCircle.visibility = View.GONE
                viewHolder.downloadBtn.visibility = View.VISIBLE
                viewHolder.downloadBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_download)
                )
            }
            "downloading" -> {
                viewHolder.downloadBtn.visibility = View.GONE
                viewHolder.downloadingCircle.visibility = View.VISIBLE
            }
        }
    }

    private fun download(item: BooksDB) {
        val downloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(item.url)
        val request: DownloadManager.Request = DownloadManager.Request(uri)
        request.setTitle(getTitle(item))
        FileUtils.createDir(context, prefix)
        request.setDestinationInExternalFilesDir(context, prefix, "${item.id}.json")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        val downloadId = downloadManager.enqueue(request)

        downloadingIds[downloadId] = item.id
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            try {
                val id = downloadingIds[downloadId]!!
                downloadingIds.remove(downloadId)
                notifyItemChanged(id)
            } catch (e: RuntimeException) {
                notifyDataSetChanged()
            }
        }
    }

    private fun getTitle(item: BooksDB): String {
        return if (language == "en")
            item.titleEn
        else
            item.title
    }

    fun registerReceiver() {
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(onComplete)
    }

}