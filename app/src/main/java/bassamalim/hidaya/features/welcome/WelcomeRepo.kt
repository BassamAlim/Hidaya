package bassamalim.hidaya.features.welcome

import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class WelcomeRepo @Inject constructor(
    private val preferencesDS: PreferencesDataSource
) {

    fun unsetFirstTime() {
        preferencesDS.setBoolean(Preference.FirstTime, false)
    }

}