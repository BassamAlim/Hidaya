package bassamalim.hidaya.features.onboarding.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import javax.inject.Inject

class OnboardingDomain @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun setLanguage(language: Language) {
        appSettingsRepository.setLanguage(language)
    }

    suspend fun setNumeralsLanguage(numeralsLanguage: Language) {
        appSettingsRepository.setNumeralsLanguage(numeralsLanguage)
    }

    suspend fun setTimeFormat(timeFormat: TimeFormat) {
        appSettingsRepository.setTimeFormat(timeFormat)
    }

    suspend fun setTheme(theme: Theme) {
        appSettingsRepository.setTheme(theme)
    }

    suspend fun unsetFirstTime() {
        appStateRepository.setIsOnboardingCompleted(true)
    }

}