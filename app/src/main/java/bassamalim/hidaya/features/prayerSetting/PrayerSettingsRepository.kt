package bassamalim.hidaya.features.prayerSetting

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import javax.inject.Inject

class PrayerSettingsRepository @Inject constructor(
    private val res: Resources,
    private val preferencesDS: PreferencesDataSource
) {

    fun numeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getNotificationType(pid: PID) =
        NotificationType.valueOf(
            preferencesDS.getString(Preference.NotificationType(pid))
        )

    fun getTimeOffset(pid: PID) =
        preferencesDS.getInt(Preference.TimeOffset(pid))

    fun getPrayerName(pid: PID) =
        res.getStringArray(R.array.prayer_names)[pid.ordinal]!!

}