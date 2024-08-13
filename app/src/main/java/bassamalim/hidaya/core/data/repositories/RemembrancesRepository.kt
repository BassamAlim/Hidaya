package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.database.daos.RemembranceCategoriesDao
import bassamalim.hidaya.core.data.database.daos.RemembrancePassagesDao
import bassamalim.hidaya.core.data.database.daos.RemembrancesDao
import bassamalim.hidaya.core.data.preferences.dataSources.RemembrancePreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemembrancesRepository @Inject constructor(
    private val remembrancePreferencesDataSource: RemembrancePreferencesDataSource,
    private val remembranceCategoriesDao: RemembranceCategoriesDao,
    private val remembrancesDao: RemembrancesDao,
    private val remembrancePassagesDao: RemembrancePassagesDao
) {

    fun getAllRemembranceCategories() = remembranceCategoriesDao.getAll()

    fun getRemembranceCategoryName(id: Int, language: Language) =
        if (language == Language.ARABIC) remembranceCategoriesDao.getNameAr(id)
        else remembranceCategoriesDao.getNameEn(id)

    fun observeAllRemembrances() = remembrancesDao.observeAll()

    fun observeRemembranceFavorites() = remembrancesDao.observeFavorites()

    fun observeRemembranceIsFavorites() = remembrancesDao.observeIsFavorites()

    fun observeCategoryRemembrances(categoryId: Int) =
        remembrancesDao.observeCategoryRemembrances(categoryId)

    fun getRemembranceNames(language: Language) =
        if (language == Language.ARABIC) remembrancesDao.getNamesAr()
        else remembrancesDao.getNamesEn()

    fun getRemembranceName(id: Int, language: Language) =
        if (language == Language.ARABIC) remembrancesDao.getNameAr(id)
        else remembrancesDao.getNameEn(id)

    suspend fun setRemembranceFavorite(id: Int, value: Int) {
        remembrancesDao.setIsFavorite(id, value)

        setBackupFavorites(
            remembrancesDao.observeIsFavorites().first().mapIndexed { index, isFavorite ->
                index to isFavorite
            }.toMap()
        )
    }

    fun getRemembrancePassages(remembranceId: Int) =
        remembrancePassagesDao.getRemembrancePassages(remembranceId)

    fun getFavorites() = remembrancePreferencesDataSource.flow.map {
        it.favorites
    }

    private suspend fun setBackupFavorites(favorites: Map<Int, Int>) {
        remembrancePreferencesDataSource.update { it.copy(
            favorites = favorites.toPersistentMap()
        )}
    }

    fun getTextSize() = remembrancePreferencesDataSource.flow.map {
        it.textSize
    }

    suspend fun setTextSize(textSize: Float) {
        remembrancePreferencesDataSource.update { it.copy(
            textSize = textSize
        )}
    }

}