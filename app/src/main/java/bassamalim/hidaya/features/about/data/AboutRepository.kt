package bassamalim.hidaya.features.about.data

import bassamalim.hidaya.core.data.preferences.dataSources.AppStatePreferencesDataSource
import javax.inject.Inject

class AboutRepository @Inject constructor(
    private val appStatePrefsRepo: AppStatePreferencesDataSource
) {

    fun getLastUpdate() = appStatePrefsRepo.getLastDailyUpdateMillis()

}