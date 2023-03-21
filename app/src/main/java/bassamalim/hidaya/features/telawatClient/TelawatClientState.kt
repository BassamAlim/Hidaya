package bassamalim.hidaya.features.telawatClient

import android.support.v4.media.session.PlaybackStateCompat
import bassamalim.hidaya.core.enums.DownloadState

data class TelawatClientState(
    val repeat: Int = PlaybackStateCompat.REPEAT_MODE_NONE,
    val shuffle: Int = PlaybackStateCompat.SHUFFLE_MODE_NONE,
    val duration: String = "00:00",
    val progress: String = "00:00",
    val secondaryProgress: Long = 0,
    val btnState: Int = PlaybackStateCompat.STATE_NONE,
    val suraName: String = "",
    val versionName: String = "",
    val reciterName: String = "",
    val controlsEnabled: Boolean = false,
    val downloadState: DownloadState = DownloadState.NotDownloaded,
)
