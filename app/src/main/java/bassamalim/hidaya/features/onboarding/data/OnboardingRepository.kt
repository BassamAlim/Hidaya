package bassamalim.hidaya.features.onboarding.data

import bassamalim.hidaya.core.data.preferences.dataSources.AppStatePreferencesDataSource
import javax.inject.Inject

class OnboardingRepository @Inject constructor(
    private val appStatePrefsRepo: AppStatePreferencesDataSource
) {

    suspend fun unsetFirstTime() {
        appStatePrefsRepo.update { it.copy(
            isOnboardingCompleted = true
        )}
    }

}