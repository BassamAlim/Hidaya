package bassamalim.hidaya.features.onboarding.domain

import bassamalim.hidaya.core.data.repositories.AppStateRepository
import javax.inject.Inject

class OnboardingDomain @Inject constructor(
    private val appStateRepo: AppStateRepository
) {

    suspend fun unsetFirstTime() {
        appStateRepo.setIsOnboardingCompleted(true)
    }

}