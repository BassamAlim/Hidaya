package bassamalim.hidaya.core.data.preferences.dataStore.objects

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class BooksPreferences(
    val textSize: Float = 15f,
    val searcherMaxMatches: Int = 10,
    val chaptersFavs: PersistentMap<Int, PersistentList<Int>> = persistentMapOf(),
    val shouldShowTutorial: Boolean = true,
    val searchSelections: PersistentList<Boolean> = persistentListOf(),
)