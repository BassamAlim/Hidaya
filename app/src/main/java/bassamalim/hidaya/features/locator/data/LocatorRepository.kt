package bassamalim.hidaya.features.locator.data

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.UserPreferencesRepository
import bassamalim.hidaya.core.models.Location
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocatorRepository @Inject constructor(
    private val db: AppDatabase,
    private val appSettingsPrefsRepo: AppSettingsPreferencesRepository,
    private val userPrefsRepo: UserPreferencesRepository
) {

    suspend fun getLanguage() = appSettingsPrefsRepo.getLanguage().first()

    suspend fun setLocation(location: Location) {
        userPrefsRepo.update { it.copy(
            location = location
        )}
    }

    fun getClosestCity(latitude: Double, longitude: Double) =
        db.cityDao().getClosest(latitude, longitude)

}