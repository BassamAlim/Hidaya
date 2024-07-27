package bassamalim.hidaya.features.onboarding.data

import bassamalim.hidaya.core.data.preferences.repositories.AppStatePreferencesRepository
import javax.inject.Inject

class OnboardingRepository @Inject constructor(
    private val appStatePrefsRepo: AppStatePreferencesRepository
) {

    suspend fun unsetFirstTime() {
        appStatePrefsRepo.update { it.copy(
            isOnboardingCompleted = true
        )}
    }

}