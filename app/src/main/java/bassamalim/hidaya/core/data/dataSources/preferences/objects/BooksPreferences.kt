package bassamalim.hidaya.core.data.dataSources.preferences.objects

import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.IntBooleanPersistentMapSerializer
import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.booksPreferences.ChapterFavoritesSerializer
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class BooksPreferences(
    @Serializable(with = ChapterFavoritesSerializer::class)
    val chapterFavorites: PersistentMap<Int, PersistentMap<Int, Boolean>> = persistentMapOf(),
    val textSize: Float = 15f,
    @Serializable(with = IntBooleanPersistentMapSerializer::class)
    val searchSelections: PersistentMap<Int, Boolean> = persistentMapOf(),
    val searchMaxMatches: Int = 10,
    val shouldShowTutorial: Boolean = true,
)