package bassamalim.hidaya.features.home

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
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

    fun getIsWerdDone() =
        PrefUtils.getBoolean(sp, Prefs.WerdDone)

    fun getPrayerNames(): Array<String> =
        resources.getStringArray(R.array.prayer_names)

    fun getNumeralsLanguage() = Language.valueOf(
        PrefUtils.getString(sp, Prefs.NumeralsLanguage)
    )

    fun getTodayWerdPage() =
        PrefUtils.getInt(sp, Prefs.WerdPage)

    fun getQuranPagesRecord() =
        PrefUtils.getInt(sp, Prefs.QuranPagesRecord)

    fun getTelawatPlaybackRecord() =
        PrefUtils.getLong(sp, Prefs.TelawatPlaybackRecord)

    fun getLocation() =
        LocUtils.retrieveLocation(sp)

}