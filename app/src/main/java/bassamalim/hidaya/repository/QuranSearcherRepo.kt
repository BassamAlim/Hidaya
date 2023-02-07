package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class QuranSearcherRepo @Inject constructor(
    private val context: Context,
    private val pref: SharedPreferences,
    private val db: AppDatabase
) {

    val language = PrefUtils.getLanguage(pref)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    fun getAyat() = db.ayahDao().getAll()

    fun getSuraNames() = db.suarDao().getNames()
    fun getSuraNamesEn() = db.suarDao().getNamesEn()

    fun getMaxMatchesItems(): Array<String> {
        return context.resources.getStringArray(R.array.searcher_matches_en)
    }

    fun getMaxMatchesIndex() = PrefUtils.getInt(pref, Prefs.QuranSearcherMaxMatchesIndex)
    fun setMaxMatchesIndex(index: Int) {
        pref.edit()
            .putInt(Prefs.QuranSearcherMaxMatchesIndex.key, index)
            .apply()
    }

}