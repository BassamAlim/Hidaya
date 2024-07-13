package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.PrayersPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PrayersPreferencesRepository(
    private val dataStore: DataStore<PrayersPreferences>
) {

    private val flow: Flow<PrayersPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(PrayersPreferences())
            else throw exception
        }

    suspend fun update(update: (PrayersPreferences) -> PrayersPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

    fun getPrayerTimesCalculatorSettings() = flow.map { it.prayerTimesCalculatorSettings }
    fun getTimeOffsets() = flow.map { it.timeOffsets }
    fun getAthanVoiceId() = flow.map { it.athanVoiceId }
    fun getShouldShowTutorial() = flow.map { it.shouldShowTutorial }

}