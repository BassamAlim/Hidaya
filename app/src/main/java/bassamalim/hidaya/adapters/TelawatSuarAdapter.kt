package bassamalim.hidaya.adapters

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.TelawatVersionsDB
import bassamalim.hidaya.models.ReciterSura
import bassamalim.hidaya.other.Utils
import com.google.gson.Gson
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class TelawatSuarAdapter(
    private val context: Context, private val original: ArrayList<ReciterSura>,
    reciterId: Int, private val versionId: Int
) : RecyclerView.Adapter<TelawatSuarAdapter.ViewHolder?>() {

    private val db: AppDatabase = Room.databaseBuilder(
        context, AppDatabase::class.java, "HidayaDB")
        .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    private val items = ArrayList(original)
    private val ver: TelawatVersionsDB
    private val downloaded = BooleanArray(114)
    private val prefix: String

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

        init {
            card = view.findViewById(R.id.sura_model_card)
            namescreen = view.findViewById(R.id.sura_namescreen)
            favBtn = view.findViewById(R.id.sura_fav_btn)
            downloadBtn = view.findViewById(R.id.download_btn)
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

        if (downloaded[suraNum]) viewHolder.downloadBtn.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_downloaded)
        )
        else viewHolder.downloadBtn.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_download)
        )

        viewHolder.downloadBtn.setOnClickListener {
            if (downloaded[items[position].getNum()]) {
                val num: Int = items[position].getNum()
                val postfix = "$prefix/$num.mp3"
                Utils.deleteFile(context, postfix)

                downloaded[num] = false
                viewHolder.downloadBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_download)
                )
            }
            else {
                download(items[position].getNum())

                viewHolder.downloadBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_downloaded)
                )
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
        val server: String = ver.getUrl()
        val link = String.format(Locale.US, "%s/%03d.mp3", server, num + 1)

        val downloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(link)
        val request: DownloadManager.Request = DownloadManager.Request(uri)
        val title = context.getString(R.string.downloading) + " " + items[num].getSearchName()
        request.setTitle(title)
        val postfix = "/Telawat/" + ver.getReciterId() + "/" + versionId
        Utils.createDir(context, postfix)
        request.setDestinationInExternalFilesDir(context, postfix, "$num.mp3")
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        downloadManager.enqueue(request)

        downloaded[num] = true
    }

    private fun updateFavorites() {
        val favSuras: Array<Any> = db.suarDao().getFav().toTypedArray()

        val surasJson: String = gson.toJson(favSuras)

        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString("favorite_suras", surasJson)
        editor.apply()
    }

}