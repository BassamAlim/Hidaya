package bassamalim.hidaya.state

import android.support.v4.media.session.PlaybackStateCompat
import bassamalim.hidaya.enum.QViewType
import bassamalim.hidaya.models.Ayah

data class QuranViewerState(
    val pageNum: Int = 0,
    val juzNum: Int = 0,
    val suraName: String = "",
    val ayas: List<Ayah> = emptyList(),
    val viewType: QViewType = QViewType.Page,
    val textSize: Int = 15,
    val playerState: Int = PlaybackStateCompat.STATE_STOPPED,
    val isBookmarked: Boolean = false,
    val infoDialogShown: Boolean = false,
    val infoDialogText: String = "",
    val settingsDialogShown: Boolean = false,
    val tutorialDialogShown: Boolean = false
)