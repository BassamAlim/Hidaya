package bassamalim.hidaya.features.quranSearcher

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class QuranSearcherRepo @Inject constructor(
    private val resources: Resources,
    private val pref: SharedPreferences,
    private val db: AppDatabase
) {

    val language = PrefUtils.getLanguage(pref)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    fun getAyat() = db.ayatDao().getAll()

    fun getSuraNames() = db.suarDao().getNames()
    fun getSuraNamesEn() = db.suarDao().getNamesEn()

    fun getMaxMatchesIndex() = PrefUtils.getInt(pref, Prefs.QuranSearcherMaxMatchesIndex)
    fun setMaxMatchesIndex(index: Int) {
        pref.edit()
            .putInt(Prefs.QuranSearcherMaxMatchesIndex.key, index)
            .apply()
    }

    fun getMaxMatchesItems(): Array<String> = resources.getStringArray(R.array.searcher_matches_en)

}