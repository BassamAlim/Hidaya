package bassamalim.hidaya.features.quran.reader.versePlayer

import android.media.MediaPlayer

data class AlternatePlayer(
    val mp: MediaPlayer,
    var state: PlayerState = PlayerState.NONE,
    var verseIdx: Int = 0,
    var repeated: Int = 0
)