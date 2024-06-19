package bassamalim.hidaya.core.data.preferences.objects

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class BooksPreferences(
    val textSize: Float = 15f,
    val searcherMaxMatches: Int = 10,
    val chaptersFavs: PersistentMap<Int, PersistentMap<Int, Int>> = persistentMapOf(),
    val shouldShowTutorial: Boolean = true,
    val searchSelections: PersistentMap<Int, Boolean> = persistentMapOf(),
)