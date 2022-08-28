package bassamalim.hidaya.adapters

import android.app.DownloadManager
import android.content.*
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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.database.dbs.TelawatVersionsDB
import bassamalim.hidaya.models.ReciterSura
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.FileUtils
import com.google.gson.Gson
import java.io.File
import java.util.*

class TelawatSuarAdapter(
    private val context: Context, private val original: ArrayList<ReciterSura>,
    reciterId: Int, private val versionId: Int
) : RecyclerView.Adapter<TelawatSuarAdapter.ViewHolder?>() {

    private val db = DBUtils.getDB(context)
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    private val items = ArrayList(original)
    private val ver: TelawatVersionsDB
    private val downloaded = BooleanArray(114)
    private val prefix: String
    private val downloadingIds = HashMap<Long, Int>()

    init {
        ver = db.telawatVersionsDao().getVersion(reciterId, versionId)
        prefix = "/Telawat/" + ver.getReciterId() + "/" + versionId
        checkDownloaded()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView
        val namescreen: TextView
        val favBtn: ImageButton
        val downloadBtn: ImageButton
        val downloadingCircle: ProgressBar

        init {
            card = view.findViewById(R.id.sura_model_card)
            namescreen = view.findViewById(R.id.sura_namescreen)
            favBtn = view.findViewById(R.id.sura_fav_btn)
            downloadBtn = view.findViewById(R.id.download_btn)
            downloadingCircle = view.findViewById(R.id.buffering_circle)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_telawat_sura, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.namescreen.text = items[position].getSurahName()
        viewHolder.card.setOnClickListener(items[position].getListener())

        doFavorite(viewHolder, position)

        doDownloaded(viewHolder, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun filter(text: String) {
        items.clear()
        if (text.isEmpty()) items.addAll(original)
        else {
            for (reciterCard in original) {
                if (reciterCard.getSearchName().contains(text)) items.add(reciterCard)
            }
        }
        notifyDataSetChanged()
    }

    private fun doFavorite(viewHolder: ViewHolder, position: Int) {
        val card: ReciterSura = items[position]

        val fav: Int = card.getFavorite()
        if (fav == 0)
            viewHolder.favBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_star_outline)
            )
        else if (fav == 1)
            viewHolder.favBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_star)
            )

        viewHolder.favBtn.setOnClickListener {
            if (card.getFavorite() == 0) {
                db.suarDao().setFav(card.getNum(), 1)
                card.setFavorite(1)
            }
            else if (card.getFavorite() == 1) {
                db.suarDao().setFav(card.getNum(), 0)
                card.setFavorite(0)
            }
            notifyItemChanged(position)

            updateFavorites()
        }
    }

    private fun doDownloaded(viewHolder: ViewHolder, position: Int) {
        val suraNum: Int = items[position].getNum()

        if (downloaded[suraNum]) {
            if (downloadingIds.containsValue(suraNum)) updateUI(viewHolder, "downloading")
            else updateUI(viewHolder, "downloaded")
        }
        else updateUI(viewHolder, "not downloaded")

        viewHolder.downloadBtn.setOnClickListener {
            if (downloadingIds.containsValue(items[position].getNum()))
                Toast.makeText(
                    context, context.getString(R.string.wait_for_download), Toast.LENGTH_SHORT
                ).show()
            else if (downloaded[items[position].getNum()]) {
                val num: Int = items[position].getNum()
                val postfix = "$prefix/$num.mp3"
                FileUtils.deleteFile(context, postfix)

                downloaded[num] = false
                updateUI(viewHolder, "not downloaded")
            }
            else {
                download(items[position].getNum())
                updateUI(viewHolder, "downloading")
            }
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

    private fun checkDownloaded() {
        val dir = File(context.getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return

        val files = dir.listFiles()
        for (element in files!!) {
            val name = element.name
            val n = name.substring(0, name.length - 4)
            try {
                val num = n.toInt()
                downloaded[num] = true
            } catch (ignored: NumberFormatException) {}
        }
    }

    private fun download(num: Int) {
        val server = ver.getUrl()
        val link = String.format(Locale.US, "%s/%03d.mp3", server, num + 1)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(link)
        val request = DownloadManager.Request(uri)
        request.setTitle(items[num].getSearchName())
        val postfix = "/Telawat/" + ver.getReciterId() + "/" + versionId
        FileUtils.createDir(context, postfix)
        request.setDestinationInExternalFilesDir(context, postfix, "$num.mp3")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        val downloadId = downloadManager.enqueue(request)

        downloadingIds[downloadId] = num

        downloaded[num] = true
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

    private fun updateFavorites() {
        val favSuras = db.suarDao().getFav().toTypedArray()

        val surasJson = gson.toJson(favSuras)

        val editor = pref.edit()
        editor.putString("favorite_suras", surasJson)
        editor.apply()
    }

    fun registerReceiver() {
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun unregisterReceiver() {
        try {
            context.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

}