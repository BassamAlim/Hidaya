package bassamalim.hidaya.features.prayerReminder

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.PID
import javax.inject.Inject

class PrayerReminderRepository @Inject constructor(
    private val res: Resources,
    private val preferencesDS: PreferencesDataSource
) {

    fun numeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getOffset(pid: PID) =
        preferencesDS.getInt(Preference.ReminderOffset(pid))

    fun getPrayerName(pid: PID) =
        res.getStringArray(R.array.prayer_names)[pid.ordinal]!!

}