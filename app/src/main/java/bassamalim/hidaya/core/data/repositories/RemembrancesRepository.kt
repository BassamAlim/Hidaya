package bassamalim.hidaya.core.data.repositories

import android.util.Log
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.RemembrancePreferencesDataSource
import bassamalim.hidaya.core.data.dataSources.room.daos.RemembranceCategoriesDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RemembrancePassagesDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RemembrancesDao
import bassamalim.hidaya.core.data.dataSources.room.entities.Remembrance
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.other.Global
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemembrancesRepository @Inject constructor(
    private val remembrancePreferencesDataSource: RemembrancePreferencesDataSource,
    private val remembranceCategoriesDao: RemembranceCategoriesDao,
    private val remembrancesDao: RemembrancesDao,
    private val remembrancePassagesDao: RemembrancePassagesDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope
) {

    suspend fun getRemembranceCategoryName(id: Int, language: Language) = withContext(dispatcher) {
        if (language == Language.ARABIC) remembranceCategoriesDao.getNameAr(id)
        else remembranceCategoriesDao.getNameEn(id)
    }

    fun observeAllRemembrances(): Flow<List<Remembrance>> {
        val items = remembrancesDao.observeAll()
        Log.d(Global.TAG, "observeAllRemembrances: $items")
        return items
    }

    fun observeFavorites() = remembrancesDao.observeFavorites()

    fun observeCategoryRemembrances(categoryId: Int) =
        remembrancesDao.observeCategoryRemembrances(categoryId)

    fun setFavorite(id: Int, value: Boolean) {
        scope.launch {
            withContext(dispatcher) {
                remembrancesDao.setFavoriteStatus(id = id, value = if (value) 1 else 0)
            }

            setFavoritesBackup(
                remembrancesDao.observeFavoriteStatuses().first().mapIndexed { index, isFavorite ->
                    index to (isFavorite == 1)
                }.toMap()
            )
        }
    }

    suspend fun setFavorites(favorites: Map<Int, Boolean>) {
        withContext(dispatcher) {
            favorites.forEach { (id, value) ->
                remembrancesDao.setFavoriteStatus(id = id, value = if (value) 1 else 0)
            }
        }
    }

    fun getFavoritesBackup() = remembrancePreferencesDataSource.getFavorites()

    private suspend fun setFavoritesBackup(favorites: Map<Int, Boolean>) {
        remembrancePreferencesDataSource.updateFavorites(favorites.toPersistentMap())
    }

    suspend fun getRemembranceName(id: Int, language: Language) = withContext(dispatcher) {
        if (language == Language.ARABIC) remembrancesDao.getNameAr(id)
        else remembrancesDao.getNameEn(id)
    }

    suspend fun getRemembrancePassages(remembranceId: Int) = withContext(dispatcher) {
        remembrancePassagesDao.getRemembrancePassages(remembranceId)
    }

    fun getTextSize() = remembrancePreferencesDataSource.getTextSize()

    suspend fun setTextSize(textSize: Float) {
        remembrancePreferencesDataSource.updateTextSize(textSize)
    }

}