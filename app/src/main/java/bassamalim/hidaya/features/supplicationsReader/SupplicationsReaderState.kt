package bassamalim.hidaya.features.supplicationsReader

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.RemembrancePassage

data class  SupplicationsReaderState(
    val title: String = "",
    val language: Language = Language.ARABIC,
    val textSize: Float = 15f,
    val items: List<RemembrancePassage> = emptyList(),
    val infoDialogShown: Boolean = false,
    val infoDialogText: String = ""
)
