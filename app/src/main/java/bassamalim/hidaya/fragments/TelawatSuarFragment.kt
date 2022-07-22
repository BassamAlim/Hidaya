package bassamalim.hidaya.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.activities.TelawatClient
import bassamalim.hidaya.adapters.TelawatSuarAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.SuarDB
import bassamalim.hidaya.databinding.FragmentTelawatSuarBinding
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.ReciterSura
import java.io.File
import java.util.*

@RequiresApi(api = Build.VERSION_CODES.O)
class TelawatSuarFragment : Fragment {

    private var binding: FragmentTelawatSuarBinding? = null
    private var recycler: RecyclerView? = null
    private var adapter: TelawatSuarAdapter? = null
    private var reciterId = 0
    private var versionId = 0
    private var type: ListType? = ListType.All
    private lateinit var availableSurahs: String
    private lateinit var surahNames: ArrayList<String>
    private lateinit var searchNames: Array<String?>
    private lateinit var favs: List<Int>
    private lateinit var downloaded: BooleanArray

    constructor()

    constructor(type: ListType, reciterId: Int, versionId: Int) {
        this.type = type
        this.reciterId = reciterId
        this.versionId = versionId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTelawatSuarBinding.inflate(inflater, container, false)

        initRecycler()

        setSearchListeners()

        return binding!!.root
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)

        if (menuVisible) {
            if (type == ListType.Downloaded) checkDownloaded()
            setAdapter()
        }
        else adapter?.unregisterReceiver()
    }

    private val data: Unit
        get() {
            val db: AppDatabase = Room.databaseBuilder(requireContext(), AppDatabase::class.java,
                "HidayaDB").createFromAsset("databases/HidayaDB.db")
                .allowMainThreadQueries().build()

            val suras: List<SuarDB> = db.suarDao().getAll()

            surahNames = ArrayList()
            searchNames = arrayOfNulls(114)
            for (i in 0..113) {
                surahNames.add(suras[i].sura_name!!)
                searchNames[i] = suras[i].search_name
            }

            availableSurahs = db.telawatVersionsDao().getSuras(reciterId, versionId)

            favs = db.suarDao().getFav()
        }

    private fun getItems(): ArrayList<ReciterSura> {
        data;

        val items: ArrayList<ReciterSura> = ArrayList<ReciterSura>()
        for (i in 0..113) {
            if (!availableSurahs.contains("," + (i + 1) + ",")
                || type == ListType.Favorite && favs[i] == 0
                || type == ListType.Downloaded && !downloaded[i])
                continue

            val name = surahNames[i]
            val searchName = searchNames[i]

            val listener = View.OnClickListener {
                val intent = Intent(context, TelawatClient::class.java)
                intent.action = "start"

                val rId = String.format(Locale.US, "%03d", reciterId)
                val vId = String.format(Locale.US, "%02d", versionId)
                val sId = String.format(Locale.US, "%03d", i)
                val mediaId = rId + vId + sId
                intent.putExtra("media_id", mediaId)

                startActivity(intent)
            }

            items.add(ReciterSura(i, name, searchName!!, favs[i], listener))
        }
        return items
    }

    private fun initRecycler() {
        recycler = binding!!.recycler
        val layoutManager = LinearLayoutManager(context)
        recycler!!.layoutManager = layoutManager
    }

    private fun setAdapter() {
        adapter = TelawatSuarAdapter(requireContext(), getItems(), reciterId, versionId)
        recycler!!.adapter = adapter

        adapter!!.registerReceiver()
    }

    private fun setSearchListeners() {
        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter!!.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filter(newText)
                return true
            }
        })
    }

    private fun checkDownloaded() {
        downloaded = BooleanArray(114)

        val prefix = "/Telawat/$reciterId/$versionId"

        val dir = File(requireContext().getExternalFilesDir(null).toString() + prefix)
        if (!dir.exists()) return

        val files = dir.listFiles()
        for (i in 0 until files!!.size) {
            val name = files[i].name
            val n = name.substring(0, name.length - 4)
            try {
                val num = n.toInt()
                downloaded[num] = true
            } catch (ignored: NumberFormatException) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        adapter?.unregisterReceiver()
        recycler!!.adapter = null
        adapter = null
    }

}