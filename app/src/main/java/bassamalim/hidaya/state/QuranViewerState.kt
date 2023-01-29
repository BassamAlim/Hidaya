package bassamalim.hidaya.state

import bassamalim.hidaya.enum.QViewType
import bassamalim.hidaya.models.Ayah

data class QuranViewerState(
    val pageNum: Int = 0,
    val juzNum: Int = 0,
    val suraName: String = "",
    val ayas: List<Ayah> = emptyList(),
    val viewType: QViewType = QViewType.Page,
    val textSize: Int = 15,
    val playerState: Int = 1, // stopped
    val isBookmarked: Boolean = false,
    val infoDialogShown: Boolean = false,
    val infoDialogText: String = "",
    val settingsDialogShown: Boolean = false,
    val tutorialDialogShown: Boolean = false
)
