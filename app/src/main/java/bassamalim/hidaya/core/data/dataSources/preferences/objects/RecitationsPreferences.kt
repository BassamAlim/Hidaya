package bassamalim.hidaya.core.data.dataSources.preferences.objects

import android.support.v4.media.session.PlaybackStateCompat
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.recitations.recitersMenu.domain.LastPlayedMedia
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class RecitationsPreferences(
    val reciterFavorites: PersistentMap<Int, Boolean> = persistentMapOf(),
    val narrationSelections: PersistentMap<Int, Boolean> = persistentMapOf(),
    val repeatMode: Int = PlaybackStateCompat.REPEAT_MODE_NONE,
    val shuffleMode: Int = PlaybackStateCompat.SHUFFLE_MODE_NONE,
    val lastPlayedMedia: LastPlayedMedia = LastPlayedMedia(),
    val verseReciterId: Int = 13,
    val verseRepeatMode: VerseRepeatMode = VerseRepeatMode.NONE,
    val shouldStopOnSuraEnd: Boolean = false,
    val shouldStopOnPageEnd: Boolean = false,
)