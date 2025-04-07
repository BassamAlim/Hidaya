package bassamalim.hidaya.features.recitations.recitersMenu.ui

import android.support.v4.media.session.PlaybackStateCompat
import bassamalim.hidaya.features.quran.surasMenu.ui.RecitationInfo

data class RecitationRecitersMenuUiState(
    val isLoading: Boolean = true,
    val playbackRecitationInfo: RecitationInfo? = null,
    val playbackState: Int = PlaybackStateCompat.STATE_NONE,
    val searchText: String = "",
    val isFiltered: Boolean = false
)