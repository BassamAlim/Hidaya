package bassamalim.hidaya.repository

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class SettingsRepo @Inject constructor(
    private val resources: Resources,
    val pref: SharedPreferences
) {

    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)
    val timeFormat = PrefUtils.getTimeFormat(pref)

    fun getTime(pid: PID): String {
        return "${PrefUtils.getInt(pref, Prefs.ExtraNotificationHour(pid))}:" +
                "${PrefUtils.getInt(pref, Prefs.ExtraNotificationMinute(pid))}"
    }

    fun setTime(pid: PID, hour: Int, minute: Int) {
        pref.edit()
            .putInt(Prefs.ExtraNotificationHour(pid).key, hour)
            .putInt(Prefs.ExtraNotificationMinute(pid).key, minute)
            .apply()
    }

    fun getTimePickerTitleStr() = resources.getString(R.string.time_picker_title)
    fun getSelectStr() = resources.getString(R.string.select)
    fun getCancelStr() = resources.getString(R.string.cancel)

}