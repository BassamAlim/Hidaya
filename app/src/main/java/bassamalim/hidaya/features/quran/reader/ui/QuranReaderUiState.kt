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
    val fillPage: Boolean = false,
    val textSize: Float = 15f,
    val playerState: Int = PlaybackStateCompat.STATE_STOPPED,
    val bookmarkOptionButtonsExpanded: Boolean = false,
    val isTutorialDialogShown: Boolean = false
)