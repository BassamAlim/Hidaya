package bassamalim.hidaya.dialogs

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.CityAdapter
import bassamalim.hidaya.adapters.CountryAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.CityDB
import bassamalim.hidaya.database.dbs.CountryDB
import bassamalim.hidaya.databinding.DialogLocationPickerBinding
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.PrefUtils

class LocationPickerDialog : AppCompatActivity() {

    private lateinit var binding: DialogLocationPickerBinding
    private lateinit var pref: SharedPreferences
    private lateinit var db: AppDatabase
    private var recyclerView: RecyclerView? = null
    private var countryAdapter: CountryAdapter? = null
    private var cityAdapter: CityAdapter? = null
    private var mode = 0  // 0 -> country , 1 -> city
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        themeify()
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBUtils.getDB(this)

        language = PrefUtils.getLanguage(this, pref)

        initRecycler()
        fillCountries()

        setSearchListeners()
    }

    private fun themeify() {
        when (PrefUtils.getTheme(this, pref)) {
            "ThemeM" -> setTheme(R.style.RoundedDialogM)
            "ThemeR" -> setTheme(R.style.RoundedDialogM)
            else -> setTheme(R.style.RoundedDialogL)
        }
    }

    private fun initRecycler() {
        recyclerView = binding.recycler
        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
    }

    private fun fillCountries() {
        val callback = object : CountryAdapter.Callback {
            override fun choice(id: Int) {
                fillCities(id)

                binding.searchView.setQuery("", false)

                mode = 1
            }
        }

        countryAdapter = CountryAdapter(getCountries, callback, language)
        recyclerView!!.adapter = countryAdapter
    }

    private fun fillCities(countryId: Int) {
        val callback = object : CityAdapter.Callback {
            override fun choice(id: Int) {
                val intent = Intent()
                intent.putExtra("country_id", countryId)
                intent.putExtra("city_id", id)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        cityAdapter = CityAdapter(getCities(countryId), callback, db, countryId, language)
        recyclerView!!.adapter = cityAdapter
    }

    private val getCountries: List<CountryDB> get() {
        return db.countryDao().getAll()
    }

    private fun getCities(countryId: Int) : MutableList<CityDB> {
        return if (language == "en") db.cityDao().getTopEn(countryId, "").toMutableList()
        else db.cityDao().getTopAr(countryId, "").toMutableList()
    }

    private fun setSearchListeners() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (mode == 0) countryAdapter?.filter(query)
                else cityAdapter?.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (mode == 0) countryAdapter?.filter(newText)
                else cityAdapter?.filter(newText)
                return true
            }
        })
    }



}