package bassamalim.hidaya.features.quran.quranReader.versePlayer

import android.media.MediaPlayer

data class AlternatePlayer(
    val mp: MediaPlayer,
    var state: PlayerState = PlayerState.NONE,
    var verseIdx: Int = 0,
    var repeated: Int = 0
)