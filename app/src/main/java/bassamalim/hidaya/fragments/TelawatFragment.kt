package bassamalim.hidaya.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.TelawatSuarCollectionActivity
import bassamalim.hidaya.adapters.TelawatAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.TelawatDB
import bassamalim.hidaya.database.dbs.TelawatRecitersDB
import bassamalim.hidaya.databinding.FragmentTelawatBinding
import bassamalim.hidaya.dialogs.FilterDialog
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Reciter
import bassamalim.hidaya.models.Reciter.RecitationVersion
import com.google.gson.Gson
import java.io.File
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatFragment : Fragment {

    private var binding: FragmentTelawatBinding? = null
    private var pref: SharedPreferences? = null
    private var gson: Gson? = null
    private var rewayat: Array<String>? = null
    private var recycler: RecyclerView? = null
    private var adapter: TelawatAdapter? = null
    private var telawat: List<TelawatDB>? = null
    private var reciters: List<TelawatRecitersDB>? = null
    private var downloaded: BooleanArray? = null
    private var selectedRewayat: BooleanArray? = null
    private var type: ListType? = null

    constructor()

    constructor(type: ListType?) {
        this.type = type
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTelawatBinding.inflate(inflater, container, false)
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        gson = Gson()
        rewayat = resources.getStringArray(R.array.rewayat)
        reciters = data
        if (type == ListType.Downloaded) checkDownloaded()
        setupRecycler()
        filter()
        setListeners()
        return binding!!.root
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            adapter = TelawatAdapter(requireContext(), makeCards())
            recycler!!.adapter = adapter
        }
    }

    private val data: List<TelawatRecitersDB>
        get() {
            val db: AppDatabase = Room.databaseBuilder(
                requireContext().applicationContext,
                AppDatabase::class.java, "HidayaDB"
            ).createFromAsset("databases/HidayaDB.db")
                .allowMainThreadQueries().build()
            telawat = db.telawatDao().all
            return if (type == ListType.Favorite) db.telawatRecitersDao()
                .getFavorites() else db.telawatRecitersDao().getAll()
        }

    private fun checkDownloaded() {
        cleanup()
        downloaded = BooleanArray(telawat!!.size)
        val prefix = "/Telawat/"
        val dir = File(requireContext().getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return
        val files = dir.listFiles()
        if (files == null || files.isEmpty()) return
        for (element in files) {
            val name = element.name
            try {
                val num = name.toInt()
                downloaded!![num] = true
            } catch (ignored: NumberFormatException) {
            }
        }
    }

    private fun makeCards(): ArrayList<Reciter> {
        val cards: ArrayList<Reciter> = ArrayList<Reciter>()
        for (i in reciters!!.indices) {
            if (type == ListType.Downloaded && !downloaded!![i]) continue
            val reciter: TelawatRecitersDB = reciters!![i]
            val versions: List<TelawatDB> = getVersions(reciter.reciter_id)
            val versionsList: MutableList<RecitationVersion> = ArrayList<RecitationVersion>()
            for (j in versions.indices) {
                val telawa: TelawatDB = versions[j]
                val listener = View.OnClickListener { v: View ->
                    val intent = Intent(
                        v.context,
                        TelawatSuarCollectionActivity::class.java
                    )
                    intent.putExtra("reciter_id", telawa.getReciter_id())
                    intent.putExtra("reciter_name", telawa.getReciter_name())
                    intent.putExtra("version_id", telawa.getVersion_id())
                    startActivity(intent)
                }
                versionsList.add(
                    RecitationVersion(
                        telawa.getVersion_id(),
                        telawa.getUrl(), telawa.getRewaya(), telawa.getCount(),
                        telawa.getSuras(), listener
                    )
                )
            }
            cards.add(
                Reciter(
                    reciter.reciter_id, reciter.reciter_name!!,
                    reciter.favorite, versionsList
                )
            )
        }
        return cards
    }

    private fun getVersions(id: Int): List<TelawatDB> {
        val result: MutableList<TelawatDB> = ArrayList<TelawatDB>()
        for (i in telawat!!.indices) {
            val telawa: TelawatDB = telawat!![i]
            if (telawa.getReciter_id() == id) result.add(telawa)
        }
        return result
    }

    private fun setupRecycler() {
        recycler = binding!!.recycler
        val layoutManager = LinearLayoutManager(context)
        recycler!!.layoutManager = layoutManager
        adapter = TelawatAdapter(requireContext(), makeCards())
        recycler!!.adapter = adapter
    }

    private fun filter() {
        selectedRewayat = getSelectedRewayat()
        adapter!!.filter(null, selectedRewayat!!)
        for (aBoolean in selectedRewayat!!) {
            if (!aBoolean) {
                binding!!.filterIb.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.ic_filtered
                    )
                )
                break
            }
        }
    }

    private fun setListeners() {
        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter!!.filter(query, null)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filter(newText, null)
                return true
            }
        })
        binding!!.filterIb.setOnClickListener { v ->
            FilterDialog(
                requireContext(), v, resources.getString(R.string.choose_rewaya), rewayat!!,
                selectedRewayat!!, adapter!!, binding!!.filterIb, "selected_rewayat"
            )
        }
    }

    private fun getSelectedRewayat(): BooleanArray {
        val defArr = BooleanArray(rewayat!!.size)
        Arrays.fill(defArr, true)
        val defStr: String = gson!!.toJson(defArr)
        return gson!!.fromJson(pref!!.getString("selected_rewayat", defStr), BooleanArray::class.java)
    }

    private fun cleanup() {
        val path = requireContext().getExternalFilesDir(null).toString() + "/Telawat/"
        val file = File(path)
        val rFiles = file.listFiles() ?: return
        for (rFile in rFiles) {
            val rfName = rFile.name
            try {
                rfName.toInt()
                var vFiles: Array<out File>? = rFile.listFiles() ?: continue
                for (vFile in vFiles!!) {
                    if (vFile.listFiles()!!.isEmpty()) vFile.delete()
                }
                vFiles = rFile.listFiles()
                if (vFiles == null) continue
                if (vFiles.isEmpty()) rFile.delete()
            } catch (ignored: NumberFormatException) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        recycler!!.adapter = null
        adapter = null
    }
}