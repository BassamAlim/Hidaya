package bassamalim.hidaya.screens

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.TelawatSuarCollectionActivity
import bassamalim.hidaya.adapters.TelawatAdapter
import bassamalim.hidaya.database.dbs.TelawatDB
import bassamalim.hidaya.database.dbs.TelawatRecitersDB
import bassamalim.hidaya.databinding.FragmentTelawatBinding
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Reciter
import bassamalim.hidaya.models.Reciter.RecitationVersion
import bassamalim.hidaya.utils.DBUtils
import com.google.gson.Gson
import java.io.File
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatFragment : Fragment {

    private var binding: FragmentTelawatBinding? = null
    private lateinit var pref: SharedPreferences
    private var gson: Gson = Gson()
    private lateinit var rewayat: Array<String>
    private var recycler: RecyclerView? = null
    private var adapter: TelawatAdapter? = null
    private lateinit var telawat: List<TelawatDB>
    private lateinit var reciters: List<TelawatRecitersDB>
    private lateinit var downloaded: BooleanArray
    private lateinit var selectedRewayat: BooleanArray
    private var type: ListType? = ListType.All

    constructor()

    constructor(type: ListType) {
        this.type = type
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cleanup()

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        rewayat = resources.getStringArray(R.array.rewayat)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTelawatBinding.inflate(inflater, container, false)

        initRecycler()

        setListeners()

        return binding!!.root
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)

        if (menuVisible) {
            reciters = data
            if (type == ListType.Downloaded) checkDownloaded()
            setAdapter()
            filter()
        }
    }

    private val data: List<TelawatRecitersDB>
        get() {
            val db = DBUtils.getDB(requireContext())

            telawat = db.telawatDao().all

            return if (type == ListType.Favorite) db.telawatRecitersDao().getFavorites()
            else db.telawatRecitersDao().getAll()
        }

    private fun checkDownloaded() {
        downloaded = BooleanArray(telawat.size)

        val prefix = "/Telawat/"

        val dir = File(requireContext().getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return

        val files = dir.listFiles()
        if (files == null || files.isEmpty()) return

        for (element in files) {
            try {
                val num = element.name.toInt()
                downloaded[num] = true
            } catch (ignored: NumberFormatException) {}
        }
    }

    private fun getItems(): ArrayList<Reciter> {
        val items: ArrayList<Reciter> = ArrayList<Reciter>()
        for (i in reciters.indices) {
            if (type == ListType.Downloaded && !downloaded[i]) continue

            val reciter: TelawatRecitersDB = reciters[i]
            val versions: List<TelawatDB> = getVersions(reciter.reciter_id)
            val versionsList: MutableList<RecitationVersion> = ArrayList<RecitationVersion>()

            for (j in versions.indices) {
                val telawa: TelawatDB = versions[j]

                val listener = View.OnClickListener { v: View ->
                    val intent = Intent(v.context, TelawatSuarCollectionActivity::class.java)
                    intent.putExtra("reciter_id", telawa.getReciterId())
                    intent.putExtra("reciter_name", telawa.getReciterName())
                    intent.putExtra("version_id", telawa.getVersionId())
                    startActivity(intent)
                }

                versionsList.add(
                    RecitationVersion(
                        telawa.getVersionId(), telawa.getUrl(), telawa.getRewaya(),
                        telawa.getCount(), telawa.getSuras(), listener
                    )
                )
            }
            items.add(
                Reciter(reciter.reciter_id, reciter.reciter_name!!, reciter.favorite, versionsList)
            )
        }
        return items
    }

    private fun getVersions(id: Int): List<TelawatDB> {
        val result: MutableList<TelawatDB> = ArrayList<TelawatDB>()
        for (i in telawat.indices) {
            val telawa: TelawatDB = telawat[i]
            if (telawa.getReciterId() == id) result.add(telawa)
        }
        return result
    }

    private fun initRecycler() {
        recycler = binding!!.recycler
        val layoutManager = LinearLayoutManager(context)
        recycler!!.layoutManager = layoutManager
    }

    private fun setAdapter() {
        adapter = TelawatAdapter(requireContext(), getItems())
        recycler!!.adapter = adapter
    }

    private fun filter() {
        selectedRewayat = getSelectedRewayat()
        adapter?.filter(null, selectedRewayat)
        for (aBoolean in selectedRewayat) {
            if (!aBoolean) {
                /*binding!!.filterIb.setImageDrawable(
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_filtered)
                )*/
                break
            }
        }
    }

    private fun setListeners() {
        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter?.filter(query, null)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter?.filter(newText, null)
                return true
            }
        })

        binding!!.filterIb.setOnClickListener { v ->
            /*FilterDialog(
                requireContext(), v, resources.getString(R.string.choose_rewaya), rewayat,
                selectedRewayat, adapter!!, binding!!.filterIb, "selected_rewayat"
            )*/
        }
    }

    private fun getSelectedRewayat(): BooleanArray {
        val defArr = BooleanArray(rewayat.size)
        Arrays.fill(defArr, true)
        val defStr = gson.toJson(defArr)

        return gson.fromJson(pref.getString("selected_rewayat", defStr), BooleanArray::class.java)
    }

    private fun cleanup() {
        val path = requireContext().getExternalFilesDir(null).toString() + "/Telawat/"
        val file = File(path)

        val rFiles = file.listFiles() ?: return
        for (rFile in rFiles) {
            try {
                rFile.name.toInt()

                val vFiles: Array<out File> = rFile.listFiles() ?: continue
                for (vFile in vFiles) {
                    if (vFile.listFiles()!!.isEmpty()) vFile.delete()
                }

                if (rFile.listFiles()!!.isEmpty()) rFile.delete()
            } catch (ignored: NumberFormatException) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        recycler!!.adapter = null
        adapter = null
    }

}