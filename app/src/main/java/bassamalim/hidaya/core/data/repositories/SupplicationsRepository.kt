package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.SupplicationsPreferencesDataSource
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SupplicationsRepository @Inject constructor(
    private val supplicationsPreferencesDataSource: SupplicationsPreferencesDataSource
) {

    fun getFavorites() = supplicationsPreferencesDataSource.flow.map {
        it.favorites
    }
    suspend fun updateFavorites(favorites: Map<Int, Int>) {
        supplicationsPreferencesDataSource.update { it.copy(
            favorites = favorites.toPersistentMap()
        )}
    }

    fun getTextSize() = supplicationsPreferencesDataSource.flow.map {
        it.textSize
    }
    suspend fun updateTextSize(textSize: Float) {
        supplicationsPreferencesDataSource.update { it.copy(
            textSize = textSize
        )}
    }

}