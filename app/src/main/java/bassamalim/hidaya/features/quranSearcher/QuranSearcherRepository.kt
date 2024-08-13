package bassamalim.hidaya.features.quranSearcher

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class QuranSearcherRepository @Inject constructor(
    private val resources: Resources,
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getLanguage() = preferencesDS.getLanguage()

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getAyat() = db.versesDao().getAll()

    fun getSuraNames() = db.surasDao().getDecoratedNamesAr()

    fun getSuraNamesEn() = db.surasDao().getDecoratedNamesEn()

    fun getMaxMatchesIndex() =
        preferencesDS.getInt(Preference.QuranSearcherMaxMatchesIndex)

    fun setMaxMatchesIndex(index: Int) {
        preferencesDS.setInt(Preference.QuranSearcherMaxMatchesIndex, index)
    }

    fun getMaxMatchesItems(): Array<String> =
        resources.getStringArray(R.array.searcher_matches_en)

}