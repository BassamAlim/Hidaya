package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.UserRecord
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) {

    fun getLocation() = userPreferencesDataSource.flow.map {
        it.location
    }
    suspend fun setLocation(location: Location) {
        userPreferencesDataSource.update { it.copy(
            location = location
        )}
    }

    fun getUserRecord() = userPreferencesDataSource.flow.map {
        it.userRecord
    }
    suspend fun setUserRecord(userRecord: UserRecord) {
        userPreferencesDataSource.update { it.copy(
            userRecord = userRecord
        )}
    }

}