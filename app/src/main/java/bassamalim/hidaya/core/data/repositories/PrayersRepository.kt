package bassamalim.hidaya.core.data.repositories

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.dataSources.PrayersPreferencesDataSource
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.models.PrayerTimesCalculatorSettings
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrayersRepository @Inject constructor(
    private val resources: Resources,
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

    fun getTimeOffset(pid: PID) = prayersPreferencesDataSource.flow.map {
            it.timeOffsets[pid]!!
        }

    suspend fun setTimeOffsets(timeOffsets: Map<PID, Int>) {
        prayersPreferencesDataSource.update { it.copy(
            timeOffsets = timeOffsets.toPersistentMap()
        )}
    }

    suspend fun setTimeOffset(pid: PID, timeOffset: Int) {
        prayersPreferencesDataSource.update { oldState -> oldState.copy(
            timeOffsets = oldState.timeOffsets.mutate {
                it[pid] = timeOffset
            }
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