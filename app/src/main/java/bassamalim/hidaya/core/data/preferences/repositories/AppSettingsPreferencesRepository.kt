package bassamalim.hidaya.core.data.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import bassamalim.hidaya.core.data.preferences.objects.AppSettingsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AppSettingsPreferencesRepository(
    private val dataStore: DataStore<AppSettingsPreferences>
) {

    private val flow: Flow<AppSettingsPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(AppSettingsPreferences())
            else throw exception
        }

    suspend fun update(update: (AppSettingsPreferences) -> AppSettingsPreferences) {
        dataStore.updateData { preferences ->
            update(preferences)
        }
    }

    fun getLanguage() = flow.map { it.language }
    fun getNumeralsLanguage() = flow.map { it.numeralsLanguage }
    fun getTheme() = flow.map { it.theme }
    fun getTimeFormat() = flow.map { it.timeFormat }
    fun getDateOffset() = flow.map { it.dateOffset }

}