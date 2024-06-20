package bassamalim.hidaya.features.onboarding

import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class OnboardingRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource
) {

    fun unsetFirstTime() {
        preferencesDS.setBoolean(Preference.FirstTime, false)
    }

}