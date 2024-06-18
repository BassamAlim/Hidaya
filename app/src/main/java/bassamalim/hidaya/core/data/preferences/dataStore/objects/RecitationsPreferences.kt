package bassamalim.hidaya.core.data.preferences.dataStore.objects

import android.support.v4.media.session.PlaybackStateCompat
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class RecitationsPreferences(
    val reciterFavorites: PersistentMap<Int, Int> = persistentMapOf(),
    val lastPlayedMediaId: String = "",
    val selectedNarrations: PersistentMap<Int, Boolean> = persistentMapOf(),
    val repeatMode: Int = PlaybackStateCompat.REPEAT_MODE_NONE,
    val shuffleMode: Int = PlaybackStateCompat.SHUFFLE_MODE_NONE,
    val lastProgress: Long = 0L,
)