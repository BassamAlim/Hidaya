package bassamalim.hidaya.features.quranReader

import android.media.MediaPlayer

data class AlternatePlayer(
    val mp: MediaPlayer,
    var state: PlayerState = PlayerState.NONE,
    var ayaIdx: Int = 0,
    var repeated: Int = 0
)