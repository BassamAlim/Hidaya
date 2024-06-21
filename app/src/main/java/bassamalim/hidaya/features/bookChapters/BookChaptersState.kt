package bassamalim.hidaya.features.bookChapters

import bassamalim.hidaya.core.enums.Language

data class BookChaptersState(
    val title: String = "",
    val favs: Map<Int, Int> = emptyMap(),
    val searchText: String = "",
    val language: Language = Language.ARABIC
)
