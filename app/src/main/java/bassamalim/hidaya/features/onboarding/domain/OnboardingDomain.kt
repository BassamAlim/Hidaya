package bassamalim.hidaya.features.onboarding.domain

import bassamalim.hidaya.features.onboarding.data.OnboardingRepository
import javax.inject.Inject

class OnboardingDomain @Inject constructor(
    private val repository: OnboardingRepository
) {

    suspend fun unsetFirstTime() {
        repository.unsetFirstTime()
    }

}