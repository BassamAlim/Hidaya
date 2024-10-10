package bassamalim.hidaya.features.books.booksMenuFilter.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BooksMenuFilterDomain @Inject constructor(
    private val booksRepository: BooksRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getOptions(language: Language) = booksRepository.getSearchSelections().map {
        it.map { (id, isSelected) ->
            id to BooksMenuFilterItem(
                name = booksRepository.getBookTitle(id, language),
                isSelected = isSelected
            )
        }.toMap()
    }.first()

    suspend fun setOptions(options: Map<Int, Boolean>) {
        booksRepository.setSearchSelections(options)
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

}