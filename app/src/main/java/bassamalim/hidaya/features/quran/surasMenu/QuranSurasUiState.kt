package bassamalim.hidaya.features.quran.surasMenu

import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.PlaybackStatus

data class QuranSurasUiState(
    val isLoading: Boolean = true,
    val bookmarks: List<BookmarkItem> = emptyList(),
    val isTutorialActive: Boolean = false,
    /** What the recitations player has loaded, null when it has nothing. */
    val playbackRecitationInfo: RecitationInfo? = null,
    val playbackState: PlaybackStatus = PlaybackStatus.CONNECTING,
    /** Non-null while the reciter picker is open. */
    val reciterPicker: ReciterPicker? = null,
)

data class ReciterPicker(
    val suraId: Int,
    val suraName: String,
    /** The recitation last listened to, when it includes the sura. */
    val lastPlayed: SuraRecitation?,
    /** Favorite reciters' recitations of the sura, without [lastPlayed]. */
    val favorites: List<SuraRecitation>
)

/** A reciter's narration that includes some sura. */
data class SuraRecitation(
    val reciterId: Int,
    val reciterName: String,
    val narrationId: Int,
    val narrationName: String,
    val server: String,
    /** Of the sura in this narration. */
    val downloadState: DownloadState = DownloadState.NOT_DOWNLOADED
)

/** [index] is the bookmark slot (0..3), which decides its color. */
data class BookmarkItem(
    val index: Int,
    val verseId: Int,
    val suraName: String,
    val verseNumText: String
)

data class SuraItem(
    val id: Int,
    val name: String,
    val numberText: String,
    val isMeccan: Boolean,
    val startPageText: String,
    val isFavorite: Boolean
)
