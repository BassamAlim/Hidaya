package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.enum.PID
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class SettingsRepo @Inject constructor(
    private val context: Context,
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

    fun getTimePickerTitleStr(): String {
        return context.getString(R.string.time_picker_title)
    }

    fun getSelectStr(): String {
        return context.getString(R.string.select)
    }

    fun getCancelStr(): String {
        return context.getString(R.string.cancel)
    }

}