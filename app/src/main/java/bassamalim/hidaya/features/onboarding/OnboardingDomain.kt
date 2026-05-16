package bassamalim.hidaya.features.onboarding

import android.app.Activity
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.utils.LangUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OnboardingDomain @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val prayersRepository: PrayersRepository
) {

    fun getLanguage() = LangUtils.getAppLanguage()

    fun setLanguage(language: Language) {
        LangUtils.setAppLanguage(language)
    }

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

    fun setNumeralsLanguage(numeralsLanguage: Language) {
        appSettingsRepository.setNumeralsLanguage(numeralsLanguage)
    }

    fun getTimeFormat() = appSettingsRepository.getTimeFormat()

    fun setTimeFormat(timeFormat: TimeFormat) {
        appSettingsRepository.setTimeFormat(timeFormat)
    }

    fun getTheme() = appSettingsRepository.getTheme()

    fun setTheme(theme: Theme) {
        appSettingsRepository.setTheme(theme)
    }

    fun getCalculationMethod() =
        prayersRepository.getPrayerTimesCalculatorSettings().map { it.calculationMethod }

    suspend fun setCalculationMethod(method: PrayerTimeCalculationMethod) {
        val current = prayersRepository.getPrayerTimesCalculatorSettings().first()
        prayersRepository.setPrayerTimesCalculatorSettings(
            current.copy(calculationMethod = method)
        )
    }

    fun unsetFirstTime() {
        appStateRepository.setOnboardingCompleted(true)
    }

    fun recreateActivity(activity: Activity) {
        activity.recreate()
    }

}