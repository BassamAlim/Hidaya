package bassamalim.hidaya.features.quranReader.ui

import android.support.v4.media.session.PlaybackStateCompat
import bassamalim.hidaya.core.models.Verse

data class QuranReaderUiState(
    val pageNumText: String = "",
    val juzNumText: String = "",
    val suraName: String = "",
    val pageVerses: List<Verse> = emptyList(),
    val trackedVerseId: Int = -1,
    val selectedVerse: Verse? = null,
    val viewType: QuranViewType = QuranViewType.PAGE,
    val textSize: Float = 15f,
    val playerState: Int = PlaybackStateCompat.STATE_STOPPED,
    val isBookmarked: Boolean = false,
    val infoDialogShown: Boolean = false,
    val infoDialogText: String = "",
    val settingsDialogShown: Boolean = false,
    val tutorialDialogShown: Boolean = false
)
