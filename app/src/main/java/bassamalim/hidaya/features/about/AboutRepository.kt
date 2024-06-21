package bassamalim.hidaya.features.about

import bassamalim.hidaya.core.data.preferences.repositories.AppStatePreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AboutRepository @Inject constructor(
    private val appStatePrefsRepo: AppStatePreferencesRepository
) {

    suspend fun getLastUpdate() =
        appStatePrefsRepo.flow.first()
            .lastDailyUpdateMillis

}