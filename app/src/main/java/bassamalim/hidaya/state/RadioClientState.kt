package bassamalim.hidaya.state

import android.support.v4.media.session.PlaybackStateCompat

data class RadioClientState(
    val btnState: Int = PlaybackStateCompat.STATE_STOPPED
)
