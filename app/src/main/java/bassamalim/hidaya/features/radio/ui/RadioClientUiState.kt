package bassamalim.hidaya.features.radio.ui

import android.support.v4.media.session.PlaybackStateCompat

data class RadioClientUiState(
    val btnState: Int = PlaybackStateCompat.STATE_STOPPED
)
