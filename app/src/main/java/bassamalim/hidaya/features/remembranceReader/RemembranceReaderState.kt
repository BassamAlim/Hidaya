package bassamalim.hidaya.features.remembranceReader

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.RemembrancePassage

data class  RemembranceReaderState(
    val title: String = "",
    val language: Language = Language.ARABIC,
    val textSize: Float = 15f,
    val items: List<RemembrancePassage> = emptyList(),
    val infoDialogShown: Boolean = false,
    val infoDialogText: String = ""
)
