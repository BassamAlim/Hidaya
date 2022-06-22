package bassamalim.hidaya.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.QuranSearcherActivity
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.adapters.QuranFragmentAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.SuarDB
import bassamalim.hidaya.databinding.FragmentQuranBinding
import bassamalim.hidaya.models.Sura
import java.util.*

class FavoriteQuranFragment : Fragment() {

    private var binding: FragmentQuranBinding? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: QuranFragmentAdapter? = null
    private lateinit var mListState: Parcelable
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var names: List<String>
    private lateinit var language: String

    companion object {
        private var mBundleRecyclerViewState: Bundle? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        language = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString(getString(R.string.language_key), getString(R.string.default_language))!!

        gridLayoutManager = GridLayoutManager(context, 1)

        binding = FragmentQuranBinding.inflate(inflater, container, false)

        setListeners()

        setupRecycler()

        setSearchListeners()

        return binding!!.root
    }

    override fun onPause() {
        super.onPause()
        mBundleRecyclerViewState = Bundle()
        mListState =
            Objects.requireNonNull<RecyclerView.LayoutManager>(recyclerView!!.layoutManager)
                .onSaveInstanceState()!!
        mBundleRecyclerViewState!!.putParcelable("recycler_state", mListState)
    }

    override fun onResume() {
        super.onResume()

        if (mBundleRecyclerViewState != null) {
            Handler().postDelayed({
                mListState = mBundleRecyclerViewState!!.getParcelable("recycler_state")!!
                Objects.requireNonNull<RecyclerView.LayoutManager>(recyclerView!!.layoutManager)
                    .onRestoreInstanceState(mListState)
            }, 50)
        }
        recyclerView!!.layoutManager = gridLayoutManager
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            adapter = QuranFragmentAdapter(requireContext(), makeCards())
            recyclerView!!.adapter = adapter
        }
    }

    private fun setListeners() {
        binding!!.fab.setOnClickListener {
            val intent = Intent(context, QuranSearcherActivity::class.java)
            startActivity(intent)
        }

        binding!!.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && binding!!.fab.isShown) binding!!.fab.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && recyclerView.canScrollVertically(1))
                    binding!!.fab.show()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun setupRecycler() {
        recyclerView = binding!!.recycler
        val layoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = layoutManager
        adapter = QuranFragmentAdapter(requireContext(), makeCards())
        recyclerView!!.adapter = adapter
    }

    private fun makeCards(): ArrayList<Sura> {
        val cards: ArrayList<Sura> = ArrayList<Sura>()
        val suras: List<SuarDB> = suras

        val surat = getString(R.string.sura)
        for (i in suras.indices) {
            val sura: SuarDB = suras[i]

            val cardListener = View.OnClickListener {
                val intent = Intent(context, QuranViewer::class.java)
                intent.action = "by_surah"
                intent.putExtra("surah_id", sura.sura_id)
                requireContext().startActivity(intent)
            }

            cards.add(
                Sura(
                    sura.sura_id, surat + " " + names[sura.sura_id],
                    sura.search_name!!, sura.tanzeel, 1, cardListener
                )
            )
        }
        return cards
    }

    private val suras: List<SuarDB>
        get() {
            val db: AppDatabase = Room.databaseBuilder(requireContext(), AppDatabase::class.java,
                "HidayaDB").createFromAsset("databases/HidayaDB.db")
                .allowMainThreadQueries().build()

            names =
                if (language == "en") db.suarDao().getNamesEn()
                else db.suarDao().getNames()

            return db.suarDao().getFavorites()
        }

    private fun setSearchListeners() {
        binding!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter!!.filterNumber(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filterName(newText)
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        recyclerView!!.adapter = null
        adapter = null
    }

}