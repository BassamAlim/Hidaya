package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.PrayersPreferencesDataSource
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.PrayerTimesCalculatorSettings
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrayersRepository @Inject constructor(
    private val prayersPreferencesDataSource: PrayersPreferencesDataSource
) {

    fun getPrayerTimesCalculatorSettings() = prayersPreferencesDataSource.flow.map {
        it.prayerTimesCalculatorSettings
    }
    suspend fun setPrayerTimesCalculatorSettings(
        prayerTimesCalculatorSettings: PrayerTimesCalculatorSettings
    ) {
        prayersPreferencesDataSource.update { it.copy(
            prayerTimesCalculatorSettings = prayerTimesCalculatorSettings
        )}
    }

    fun getTimeOffsets() = prayersPreferencesDataSource.flow.map {
        it.timeOffsets.toMap()
    }
    suspend fun setTimeOffsets(timeOffsets: Map<PID, Int>) {
        prayersPreferencesDataSource.update { it.copy(
            timeOffsets = timeOffsets.toPersistentMap()
        )}
    }

    fun getAthanVoiceId() = prayersPreferencesDataSource.flow.map {
        it.athanVoiceId
    }
    suspend fun setAthanVoiceId(athanVoiceId: Int) {
        prayersPreferencesDataSource.update { it.copy(
            athanVoiceId = athanVoiceId
        )}
    }

    fun getShouldShowTutorial() = prayersPreferencesDataSource.flow.map {
        it.shouldShowTutorial
    }
    suspend fun setShouldShowTutorial(shouldShowTutorial: Boolean) {
        prayersPreferencesDataSource.update { it.copy(
            shouldShowTutorial = shouldShowTutorial
        )}
    }

}