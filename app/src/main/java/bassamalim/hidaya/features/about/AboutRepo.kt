package bassamalim.hidaya.features.about

import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class AboutRepo @Inject constructor(
    private val preferencesDS: PreferencesDataSource
) {

    fun getLastUpdate() =
        preferencesDS.getLong(Preference.LastDailyUpdateMillis)

}