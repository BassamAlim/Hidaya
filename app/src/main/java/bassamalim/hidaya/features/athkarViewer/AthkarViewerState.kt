package bassamalim.hidaya.features.athkarViewer

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Thikr

data class  AthkarViewerState(
    val title: String = "",
    val language: Language = Language.ARABIC,
    val textSize: Float = 15f,
    val items: List<Thikr> = emptyList(),
    val infoDialogShown: Boolean = false,
    val infoDialogText: String = ""
)
