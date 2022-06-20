package bassamalim.hidaya.adapters

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.models.Reciter
import bassamalim.hidaya.models.Reciter.RecitationVersion
import bassamalim.hidaya.other.Utils
import bassamalim.hidaya.replacements.FilteredRecyclerAdapter
import com.google.gson.Gson
import java.io.File
import java.util.*

class TelawatAdapter(private val context: Context, private val original: ArrayList<Reciter>) :
    FilteredRecyclerAdapter<TelawatAdapter.ViewHolder>() {

    private val db: AppDatabase = Room.databaseBuilder(
        context, AppDatabase::class.java, "HidayaDB").createFromAsset(
            "databases/HidayaDB.db").allowMainThreadQueries().build()
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson: Gson = Gson()
    private val rewayat: Array<String>
    private val viewPool: RecycledViewPool = RecycledViewPool()
    private val items = ArrayList(original)
    private var selected: BooleanArray? = null

    init {
        rewayat = getRewayat()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reciterNamescreen: TextView
        val favBtn: ImageView
        val recyclerView: RecyclerView

        init {
            reciterNamescreen = view.findViewById(R.id.reciter_namescreen)
            favBtn = view.findViewById(R.id.telawa_fav_btn)
            recyclerView = view.findViewById(R.id.versions_recycler)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_telawat_reciter, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card: Reciter = items[position]

        viewHolder.reciterNamescreen.text = card.getName()

        doFavorite(viewHolder, position)

        setupVerRecycler(viewHolder, card)
    }

    override fun filter(text: String?, selected: BooleanArray?) {
        this.selected = selected
        items.clear()

        if (text == null) {
            for (reciterCard in original) {
                if (hasSelectedVersion(reciterCard.getVersions(), selected!!))
                    items.add(reciterCard)
            }
        }
        else {
            if (text.isEmpty()) items.addAll(original)
            else {
                for (reciterCard in original) {
                    if (reciterCard.getName().contains(text)) items.add(reciterCard)
                }
            }
        }
        notifyDataSetChanged()
    }

    private fun hasSelectedVersion(
        versions: List<RecitationVersion>, selected: BooleanArray
    ): Boolean {
        for (i in versions.indices) {
            for (j in rewayat.indices) {
                if (selected[j] && versions[i].getRewaya().startsWith(rewayat[j])) return true
            }
        }
        return false
    }

    private fun doFavorite(viewHolder: ViewHolder, position: Int) {
        val card: Reciter = items[position]

        val fav: Int = card.getFavorite()
        if (fav == 0) viewHolder.favBtn.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_star_outline)
        )
        else if (fav == 1) viewHolder.favBtn.setImageDrawable(
            AppCompatResources.getDrawable(context, R.drawable.ic_star)
        )

        viewHolder.favBtn.setOnClickListener {
            if (card.getFavorite() == 0) {
                db.telawatRecitersDao().setFav(card.getId(), 1)
                card.setFavorite(1)
            }
            else if (card.getFavorite() == 1) {
                db.telawatRecitersDao().setFav(card.getId(), 0)
                card.setFavorite(0)
            }
            notifyItemChanged(position)

            updateFavorites()
        }
    }

    private fun setupVerRecycler(viewHolder: ViewHolder, card: Reciter) {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.initialPrefetchItemCount = card.getVersions().size
        val versionsAdapter = TelawaVersionAdapter(
            context, getSelectedVersions(card.getVersions()), card.getVersions().size,
            card.getId(), db.suarDao().getNames()
        )
        viewHolder.recyclerView.layoutManager = layoutManager
        viewHolder.recyclerView.adapter = versionsAdapter
        viewHolder.recyclerView.setRecycledViewPool(viewPool)
    }

    private fun getSelectedVersions(versions: List<RecitationVersion>): List<RecitationVersion> {
        if (selected == null) return versions

        val selectedVersions: MutableList<RecitationVersion> = ArrayList()
        for (i in versions.indices) {
            for (j in rewayat.indices) {
                if (selected!![j] && versions[i].getRewaya().startsWith(rewayat[j])) {
                    selectedVersions.add(versions[i])
                    break
                }
            }
        }

        return selectedVersions
    }

    private fun updateFavorites() {
        val favReciters: List<Int?> = db.telawatRecitersDao().getFavs()

        val recitersJson: String = gson.toJson(favReciters)

        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString("favorite_reciters", recitersJson)
        editor.apply()
    }

    private fun getRewayat(): Array<String> {
        val standardResources = context.resources
        val assets: AssetManager = standardResources.assets
        val metrics: DisplayMetrics = standardResources.displayMetrics
        val config = Configuration(standardResources.configuration)
        config.setLocale(Locale.forLanguageTag("ar"))
        val arabicResources = Resources(assets, metrics, config)
        return arabicResources.getStringArray(R.array.rewayat)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

internal class TelawaVersionAdapter(
    private val context: Context, private val items: List<RecitationVersion>,
    private val fullSize: Int, private val reciterId: Int, private val names: List<String>
) : RecyclerView.Adapter<TelawaVersionAdapter.ViewHolder?>() {
    // We need the full size of the unfiltered list of versions so that
    // the downloaded wont get mixed up because of the ids change

    private var downloaded: BooleanArray? = null
    private val prefix: String = "/Telawat/$reciterId"

    init {
        checkDownloaded()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView
        val tv: TextView
        val downloadBtn: ImageButton

        init {
            cardView = view.findViewById(R.id.main_card)
            tv = view.findViewById(R.id.version_namescreen)
            downloadBtn = view.findViewById(R.id.download_btn)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_telawat_version, viewGroup, false)
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val ver: RecitationVersion = items[position]

        viewHolder.tv.text = ver.getRewaya()

        viewHolder.cardView.setOnClickListener(ver.getListener())

        doDownloaded(viewHolder, position, ver)
    }

    private fun doDownloaded(viewHolder: ViewHolder, position: Int, ver: RecitationVersion) {
        val verId: Int = items[position].getVersionId()

        if (downloaded!![verId])
            viewHolder.downloadBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_downloaded)
            )
        else
            viewHolder.downloadBtn.setImageDrawable(
                AppCompatResources.getDrawable(context, R.drawable.ic_download)
            )

        viewHolder.downloadBtn.setOnClickListener {
            if (downloaded!![verId]) {
                val postfix = prefix + "/" + ver.getVersionId()
                Utils.deleteFile(context, postfix)

                downloaded!![ver.getVersionId()] = false
                viewHolder.downloadBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_download)
                )
            }
            else {
                val runnable = Runnable { downloadVer(ver) } // run download on a background thread
                AsyncTask.execute(runnable)

                viewHolder.downloadBtn.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.ic_downloaded)
                )
            }
        }
    }

    private fun checkDownloaded() {
        downloaded = BooleanArray(fullSize)

        val dir = File(context.getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return

        val files = dir.listFiles()
        if (files == null || files.isEmpty()) return

        for (element in files) {
            val name = element.name
            try {
                val num = name.toInt()
                downloaded!![num] = true
            } catch (ignored: NumberFormatException) {}
        }
    }

    private fun downloadVer(ver: RecitationVersion) {
        val downloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var request: DownloadManager.Request

        for (i in 0..113) {
            if (ver.getSuras().contains("," + (i + 1) + ",")) {
                val link = String.format(Locale.US, "%s/%03d.mp3", ver.getServer(), i + 1)

                val uri = Uri.parse(link)
                request = DownloadManager.Request(uri)
                request.setTitle(names[i])
                val postfix = prefix + "/" + ver.getVersionId()
                Utils.createDir(context, postfix)
                request.setDestinationInExternalFilesDir(context, postfix, "$i.mp3")
                request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                downloadManager.enqueue(request)
            }
        }

        downloaded!![ver.getVersionId()] = true
    }

    override fun getItemCount(): Int {
        return items.size
    }
}