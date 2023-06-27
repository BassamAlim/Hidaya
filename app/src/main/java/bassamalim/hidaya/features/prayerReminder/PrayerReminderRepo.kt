package bassamalim.hidaya.features.prayerReminder

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class PrayerReminderRepo @Inject constructor(
    private val res: Resources,
    private val sp: SharedPreferences
) {

    val numeralsLanguage = PrefUtils.getNumeralsLanguage(sp)

    fun getOffset(pid: PID) =
        PrefUtils.getInt(sp, Prefs.ReminderOffset(pid))

    fun getPrayerName(pid: PID) =
        res.getStringArray(R.array.prayer_names)[pid.ordinal]!!

}