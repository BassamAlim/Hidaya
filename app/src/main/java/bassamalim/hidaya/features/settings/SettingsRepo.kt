package bassamalim.hidaya.features.settings

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.PID
import javax.inject.Inject

class SettingsRepo @Inject constructor(
    private val resources: Resources,
    private val preferencesDS: PreferencesDataSource
) {

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getTimeFormat() = preferencesDS.getTimeFormat()

    fun getTime(pid: PID): String {
        val minuteOfDay = preferencesDS.getInt(Preference.ExtraNotificationMinuteOfDay(pid))
        val hour = minuteOfDay / 60
        val minute = minuteOfDay % 60
        return "${hour.toString().format("%02d")}:${minute.toString().format("%02d")}"
    }

    fun setTime(pid: PID, hour: Int, minute: Int) {
        val minuteOfDay = hour * 60 + minute
        preferencesDS.setInt(Preference.ExtraNotificationMinuteOfDay(pid), minuteOfDay)
    }

    fun getTimePickerTitleStr() = resources.getString(R.string.time_picker_title)
    fun getSelectStr() = resources.getString(R.string.select)
    fun getCancelStr() = resources.getString(R.string.cancel)

}