package bassamalim.hidaya.repository

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.utils.LocUtils
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class HomeRepo @Inject constructor(
    private val resources: Resources,
    val pref: SharedPreferences,
    val db: AppDatabase
) {

    fun getIsWerdDone() = PrefUtils.getBoolean(pref, Prefs.WerdDone)

    fun getPrayerNames(): Array<String> {
        return resources.getStringArray(R.array.prayer_names)
    }

    fun getNumeralsLanguage(): Language {
        return Language.valueOf(PrefUtils.getString(pref, Prefs.NumeralsLanguage))
    }

    fun getTodayWerdPage() = PrefUtils.getInt(pref, Prefs.TodayWerdPage)

    fun getQuranPagesRecord() = PrefUtils.getInt(pref, Prefs.QuranPagesRecord)

    fun getTelawatPlaybackRecord() = PrefUtils.getLong(pref, Prefs.TelawatPlaybackRecord)

    fun getLocation() = LocUtils.retrieveLocation(pref)

}