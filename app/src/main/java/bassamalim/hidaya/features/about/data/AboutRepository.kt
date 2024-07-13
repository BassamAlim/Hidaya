package bassamalim.hidaya.features.about.data

import bassamalim.hidaya.core.data.preferences.repositories.AppStatePreferencesRepository
import javax.inject.Inject

class AboutRepository @Inject constructor(
    private val appStatePrefsRepo: AppStatePreferencesRepository
) {

    fun getLastUpdate() = appStatePrefsRepo.getLastDailyUpdateMillis()

}