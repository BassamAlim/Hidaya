package bassamalim.hidaya.features.home

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class HomeRepo @Inject constructor(
    private val resources: Resources,
    val sp: SharedPreferences,
    val db: AppDatabase
) {

    fun getIsWerdDone() = PrefUtils.getBoolean(sp, bassamalim.hidaya.core.data.Prefs.WerdDone)

    fun getPrayerNames(): Array<String> {
        return resources.getStringArray(R.array.prayer_names)
    }

    fun getNumeralsLanguage(): Language {
        return Language.valueOf(PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.NumeralsLanguage))
    }

    fun getTodayWerdPage() = PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.TodayWerdPage)

    fun getQuranPagesRecord() = PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.QuranPagesRecord)

    fun getTelawatPlaybackRecord() = PrefUtils.getLong(sp, bassamalim.hidaya.core.data.Prefs.TelawatPlaybackRecord)

    fun getLocation() = LocUtils.retrieveLocation(sp)

}