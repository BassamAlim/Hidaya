package bassamalim.hidaya.core.data.preferences.dataStore.objects

import android.support.v4.media.session.PlaybackStateCompat
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class RecitationsPreferences(
    val favoriteReciterIds: PersistentList<Int> = persistentListOf(),
    val lastPlayedMediaId: String = "",
    val selectedNarrations: PersistentList<Int> = persistentListOf(),
    val repeatMode: Int = PlaybackStateCompat.REPEAT_MODE_NONE,
    val shuffleMode: Int = PlaybackStateCompat.SHUFFLE_MODE_NONE,
    val lastProgress: Long = 0L,
)