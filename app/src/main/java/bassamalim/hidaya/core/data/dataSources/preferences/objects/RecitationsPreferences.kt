package bassamalim.hidaya.core.data.dataSources.preferences.objects

import android.support.v4.media.session.PlaybackStateCompat
import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.IntBooleanPersistentMapSerializer
import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.StringBooleanPersistentMapSerializer
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.recitations.recitersMenu.domain.LastPlayedMedia
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class RecitationsPreferences(
    @Serializable(with = IntBooleanPersistentMapSerializer::class)
    val reciterFavorites: PersistentMap<Int, Boolean> = persistentMapOf(),
    @Serializable(with = StringBooleanPersistentMapSerializer::class)
    val narrationSelections: PersistentMap<String, Boolean> = persistentMapOf(),
    val repeatMode: Int = PlaybackStateCompat.REPEAT_MODE_NONE,
    val shuffleMode: Int = PlaybackStateCompat.SHUFFLE_MODE_NONE,
    val lastPlayedMedia: LastPlayedMedia = LastPlayedMedia(),
    val verseReciterId: Int = 13,
    val verseRepeatMode: VerseRepeatMode = VerseRepeatMode.NO_REPEAT,
    val shouldStopOnSuraEnd: Boolean = false,
    val shouldStopOnPageEnd: Boolean = false,
)