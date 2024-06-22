package bassamalim.hidaya.features.dateConverter

import bassamalim.hidaya.core.enums.Language

data class DateConverterState(
    val hijriValues: List<String> = listOf("", "", ""),
    val gregorianValues: List<String> = listOf("", "", ""),
    val numeralsLanguage: Language = Language.ARABIC,
)