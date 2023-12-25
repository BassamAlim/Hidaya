package bassamalim.hidaya.features.quranViewer

import android.support.v4.media.session.PlaybackStateCompat
import bassamalim.hidaya.core.enums.QuranViewTypes
import bassamalim.hidaya.core.models.Aya

data class QuranViewerState(
    val pageNum: Int = 0,
    val juzNum: Int = 0,
    val suraName: String = "",
    val pageAyat: List<Aya> = emptyList(),
    val trackedAyaId: Int = -1,
    val selectedAya: Aya? = null,
    val viewType: QuranViewTypes = QuranViewTypes.Page,
    val textSize: Float = 15f,
    val playerState: Int = PlaybackStateCompat.STATE_STOPPED,
    val isBookmarked: Boolean = false,
    val infoDialogShown: Boolean = false,
    val infoDialogText: String = "",
    val settingsDialogShown: Boolean = false,
    val tutorialDialogShown: Boolean = false
)
