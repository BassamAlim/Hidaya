package bassamalim.hidaya.core.data.repositories

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.PrayersPreferencesDataSource
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrayersRepository @Inject constructor(
    private val resources: Resources,
    private val prayersPreferencesDataSource: PrayersPreferencesDataSource
) {

    fun getPrayerTimesCalculatorSettings() =
        prayersPreferencesDataSource.getPrayerTimeCalculatorSettings()

    suspend fun setCalculationMethod(calculationMethod: PrayerTimeCalculationMethod) {
        prayersPreferencesDataSource.updatePrayerTimeCalculatorSettings(
            getPrayerTimesCalculatorSettings().first().copy(
                calculationMethod = calculationMethod
            )
        )
    }

    suspend fun setJuristicMethod(juristicMethod: PrayerTimeJuristicMethod) {
        prayersPreferencesDataSource.updatePrayerTimeCalculatorSettings(
            getPrayerTimesCalculatorSettings().first().copy(
                juristicMethod = juristicMethod
            )
        )
    }

    suspend fun setAdjustHighLatitudes(adjustmentMethod: HighLatitudesAdjustmentMethod) {
        prayersPreferencesDataSource.updatePrayerTimeCalculatorSettings(
            getPrayerTimesCalculatorSettings().first().copy(
                highLatitudesAdjustmentMethod = adjustmentMethod
            )
        )
    }

    fun getAthanAudioId() = prayersPreferencesDataSource.getAthanAudioId()

    suspend fun setAthanAudioId(audioId: Int) {
        prayersPreferencesDataSource.updateAthanAudioId(audioId)
    }

    fun getShouldShowTutorial() = prayersPreferencesDataSource.getShouldShowTutorial()

    suspend fun setShouldShowTutorial(shouldShowTutorial: Boolean) {
        prayersPreferencesDataSource.updateShouldShowTutorial(shouldShowTutorial)
    }

    fun getPrayerNames(): Map<Prayer, String> {
        val names = resources.getStringArray(R.array.prayer_names) as Array<String>
        return mapOf(
            Prayer.FAJR to names[0],
            Prayer.SUNRISE to names[1],
            Prayer.DHUHR to names[2],
            Prayer.ASR to names[3],
            Prayer.MAGHRIB to names[4],
            Prayer.ISHAA to names[5]
        )
    }

    fun getPrayerName(prayer: Prayer) =
        resources.getStringArray(R.array.prayer_names)[prayer.ordinal]!!

}