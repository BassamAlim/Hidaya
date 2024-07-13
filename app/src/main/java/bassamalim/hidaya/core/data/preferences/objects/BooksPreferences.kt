package bassamalim.hidaya.core.data.preferences.objects

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class BooksPreferences(
    val chapterFavorites: PersistentMap<Int, PersistentMap<Int, Int>> = persistentMapOf(),
    val textSize: Float = 15f,
    val searchSelections: PersistentMap<Int, Boolean> = persistentMapOf(),
    val searchMaxMatches: Int = 10,
    val shouldShowTutorial: Boolean = true,
)