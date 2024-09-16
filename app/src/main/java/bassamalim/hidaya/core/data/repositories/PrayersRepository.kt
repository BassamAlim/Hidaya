package bassamalim.hidaya.core.data.repositories

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.PrayersPreferencesDataSource
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    fun getTimeOffsets() = prayersPreferencesDataSource.getTimeOffsets()

    fun getTimeOffset(pid: PID) = getTimeOffsets().map { it[pid]!! }

    suspend fun setTimeOffsets(timeOffsets: Map<PID, Int>) {
        prayersPreferencesDataSource.updateTimeOffsets(timeOffsets.toPersistentMap())
    }

    suspend fun setTimeOffset(pid: PID, timeOffset: Int) {
        prayersPreferencesDataSource.updateTimeOffsets(
            getTimeOffsets().first().toPersistentMap().mutate {
                it[pid] = timeOffset
            }
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

    fun getPrayerNames(): Map<PID, String> {
        val names = resources.getStringArray(R.array.prayer_names) as Array<String>
        return mapOf(
            PID.FAJR to names[0],
            PID.SUNRISE to names[1],
            PID.DHUHR to names[2],
            PID.ASR to names[3],
            PID.MAGHRIB to names[4],
            PID.ISHAA to names[5]
        )
    }

    fun getPrayerName(pid: PID) =
        resources.getStringArray(R.array.prayer_names)[pid.ordinal]!!

}