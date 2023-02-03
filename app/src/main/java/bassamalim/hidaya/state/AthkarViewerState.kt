package bassamalim.hidaya.state

import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.models.Thikr

data class AthkarViewerState(
    val title: String = "",
    val language: Language = Language.ARABIC,
    val textSize: Float = 15f,
    val items: List<Thikr> = emptyList(),
    val infoDialogShown: Boolean = false,
    val infoDialogText: String = ""
)
