package bassamalim.hidaya.features.radio

import android.support.v4.media.session.PlaybackStateCompat

data class RadioClientState(
    val btnState: Int = PlaybackStateCompat.STATE_STOPPED
)
