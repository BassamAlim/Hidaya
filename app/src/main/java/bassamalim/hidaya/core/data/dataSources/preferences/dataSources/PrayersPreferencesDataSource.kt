package bassamalim.hidaya.core.data.dataSources.preferences.dataSources

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.dataSources.preferences.objects.PrayersPreferences
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PrayersPreferencesDataSource(
    private val dataStore: DataStore<PrayersPreferences>
) {

    private val flow: Flow<PrayersPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(PrayersPreferences())
            else throw exception
        }

    fun getPrayerTimeCalculatorSettings() = flow.map { it.prayerTimeCalculatorSettings }
    suspend fun updatePrayerTimeCalculatorSettings(settings: PrayerTimeCalculatorSettings) {
        dataStore.updateData { preferences ->
            preferences.copy(prayerTimeCalculatorSettings = settings)
        }
    }

    fun getAthanAudioId() = flow.map { it.athanAudioId }
    suspend fun updateAthanAudioId(id: Int) {
        dataStore.updateData { preferences ->
            preferences.copy(athanAudioId = id)
        }
    }

    fun getShouldShowTutorial() = flow.map { it.shouldShowTutorial }
    suspend fun updateShouldShowTutorial(shouldShow: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(shouldShowTutorial = shouldShow)
        }
    }

}