package bassamalim.hidaya.features.onboarding.domain

import android.app.Activity
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.utils.ActivityUtils
import javax.inject.Inject

class OnboardingDomain @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    fun getLanguage() = appSettingsRepository.getLanguage()

    suspend fun setLanguage(language: Language) {
        appSettingsRepository.setLanguage(language)
    }

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

    suspend fun setNumeralsLanguage(numeralsLanguage: Language) {
        appSettingsRepository.setNumeralsLanguage(numeralsLanguage)
    }

    fun getTimeFormat() = appSettingsRepository.getTimeFormat()

    suspend fun setTimeFormat(timeFormat: TimeFormat) {
        appSettingsRepository.setTimeFormat(timeFormat)
    }

    fun getTheme() = appSettingsRepository.getTheme()

    suspend fun setTheme(theme: Theme) {
        appSettingsRepository.setTheme(theme)
    }

    suspend fun unsetFirstTime() {
        appStateRepository.setOnboardingCompleted(true)
    }

    fun restartActivity(activity: Activity) {
        ActivityUtils.restartActivity(activity)
    }

}