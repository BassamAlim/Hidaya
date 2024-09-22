package bassamalim.hidaya.core.data.dataSources.preferences.objects

import bassamalim.hidaya.core.data.dataSources.preferences.serializers.customSerializers.IntBooleanPersistentMapSerializer
import bassamalim.hidaya.core.models.QuranPageBookmark
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class QuranPreferences(
    @Serializable(with = IntBooleanPersistentMapSerializer::class)
    val suraFavorites: PersistentMap<Int, Boolean> = persistentMapOf(),
    val viewType: QuranViewType = QuranViewType.PAGE,
    val textSize: Float = 30f,
    val pageBookmark: QuranPageBookmark? = null,
    val searchMaxMatches: Int = 10,
    val shouldShowMenuTutorial: Boolean = true,
    val shouldShowReaderTutorial: Boolean = true,
    val werdPageNum: Int = 25,
    val isWerdDone: Boolean = false,
)