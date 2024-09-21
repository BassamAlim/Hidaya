package bassamalim.hidaya.features.remembrances.remembrancesMenu.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.features.remembrances.remembrancesMenu.ui.RemembrancesItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemembrancesMenuDomain @Inject constructor(
    private val remembrancesRepository: RemembrancesRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    fun getLanguage() = appSettingsRepository.getLanguage()

    fun getRemembrances(
        menuType: MenuType,
        categoryId: Int,
        language: Language
    ): Flow<List<RemembrancesItem>> {
        return when (menuType) {
            MenuType.ALL, MenuType.DOWNLOADED -> remembrancesRepository.observeAllRemembrances()
            MenuType.FAVORITES -> remembrancesRepository.observeFavorites()
            MenuType.CUSTOM -> remembrancesRepository.observeCategoryRemembrances(categoryId)
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

    fun setFavoriteStatus(id: Int, value: Boolean) {
        remembrancesRepository.setFavorite(id, value)
    }

}