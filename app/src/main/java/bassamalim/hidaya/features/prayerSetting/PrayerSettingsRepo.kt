package bassamalim.hidaya.features.prayerSetting

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class PrayerSettingsRepo @Inject constructor(
    private val res: Resources,
    private val sp: SharedPreferences
) {

    val language = PrefUtils.getLanguage(sp)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(sp)

    fun getNotificationType(pid: PID) = NotificationType.valueOf(
        PrefUtils.getString(sp, Prefs.NotificationType(pid))
    )

    fun getTimeOffset(pid: PID) =
        PrefUtils.getInt(sp, Prefs.TimeOffset(pid))

    fun getReminderOffset(pid: PID) =
        PrefUtils.getInt(sp, Prefs.ReminderOffset(pid))

    private fun getPrayerName(pid: PID) =
        res.getStringArray(R.array.prayer_names)[pid.ordinal]!!

}