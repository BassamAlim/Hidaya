package bassamalim.hidaya.features.about.data

import bassamalim.hidaya.core.data.preferences.repositories.AppStatePreferencesRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AboutRepository @Inject constructor(
    private val appStatePrefsRepo: AppStatePreferencesRepository
) {

    fun getLastUpdate() =
        appStatePrefsRepo.flow.map {
            it.lastDailyUpdateMillis
        }

}