package bassamalim.hidaya.features.bookChapters

import bassamalim.hidaya.core.enums.Language

data class BookChaptersState(
    val title: String = "",
    val favs: List<Int> = emptyList(),
    val searchText: String = "",
    val language: Language = Language.ARABIC
)
