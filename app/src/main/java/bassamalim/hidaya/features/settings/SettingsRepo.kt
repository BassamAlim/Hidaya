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

    fun getTime(pid: PID): String =
        "${preferencesDS.getInt(Preference.ExtraNotificationHour(pid))}:" +
                "${preferencesDS.getInt(Preference.ExtraNotificationMinute(pid))}"

    fun setTime(pid: PID, hour: Int, minute: Int) {
        preferencesDS.setInt(Preference.ExtraNotificationHour(pid), hour)
        preferencesDS.setInt(Preference.ExtraNotificationMinute(pid), minute)
    }

    fun getTimePickerTitleStr() = resources.getString(R.string.time_picker_title)
    fun getSelectStr() = resources.getString(R.string.select)
    fun getCancelStr() = resources.getString(R.string.cancel)

}