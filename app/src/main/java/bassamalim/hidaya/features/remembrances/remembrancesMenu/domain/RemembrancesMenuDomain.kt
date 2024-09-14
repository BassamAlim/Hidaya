package bassamalim.hidaya.features.remembrances.remembrancesMenu.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.features.remembrances.remembrancesMenu.RemembrancesItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemembrancesMenuDomain @Inject constructor(
    private val remembrancesRepository: RemembrancesRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    fun getRemembrances(
        type: String,
        categoryId: Int,
        language: Language?
    ): Flow<List<RemembrancesItem>> {
        if (language == null) return listOf(emptyList<RemembrancesItem>()).asFlow()

        return when (type) {
            ListType.FAVORITES.name -> remembrancesRepository.observeFavorites()
            ListType.CUSTOM.name -> remembrancesRepository.observeCategoryRemembrances(categoryId)
            else -> remembrancesRepository.observeAllRemembrances()
        }.map {
            it.map { remembrance ->
                RemembrancesItem(
                    id = remembrance.id,
                    categoryId = remembrance.categoryId,
                    name =
                        if (language == Language.ARABIC) remembrance.nameAr!!
                        else remembrance.nameEn!!,
                    isFavorite = remembrance.isFavorite == 1
                )
            }
        }
    }

    suspend fun getRemembrancePassages(remembrancesId: Int) =
        remembrancesRepository.getRemembrancePassages(remembrancesId)

    suspend fun getCategoryTitle(categoryId: Int, language: Language) =
        remembrancesRepository.getRemembranceCategoryName(categoryId, language)

    suspend fun setFavoriteStatus(id: Int, value: Boolean) {
        remembrancesRepository.setFavorite(id, value)
    }

}