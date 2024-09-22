package bassamalim.hidaya.features.quran.reader.ui

import android.support.v4.media.session.PlaybackStateCompat
import bassamalim.hidaya.core.models.Verse

data class QuranReaderUiState(
    val isLoading: Boolean = true,
    val pageNum: String = "",
    val juzNum: String = "",
    val suraName: String = "",
    val pageVerses: List<Verse> = emptyList(),
    val trackedVerseId: Int = -1,
    val selectedVerse: Verse? = null,
    val viewType: QuranViewType = QuranViewType.PAGE,
    val textSize: Float = 15f,
    val playerState: Int = PlaybackStateCompat.STATE_STOPPED,
    val isBookmarked: Boolean = false,
    val isInfoDialogShown: Boolean = false,
    val infoDialogText: String = "",
    val isSettingsDialogShown: Boolean = false,
    val isTutorialDialogShown: Boolean = false,
    val isPlayerNotSupportedShown: Boolean = false,
)