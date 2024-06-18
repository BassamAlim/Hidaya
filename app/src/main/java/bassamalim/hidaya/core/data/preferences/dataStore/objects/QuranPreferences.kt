package bassamalim.hidaya.core.data.preferences.dataStore.objects

import bassamalim.hidaya.core.enums.AyaRepeat
import bassamalim.hidaya.core.enums.QuranViewType
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Serializable
data class QuranPreferences(
    val ayaReciterId: Int = 13,
    val ayaRepeat: AyaRepeat = AyaRepeat.NONE,
    val bookmarkedPage: Int = 1,
    val bookmarkedSura: Int = 1,
    val suraFavorites: PersistentMap<Int, Int> = persistentMapOf(),
    val searcherMaxMatches: Int = 10,
    val textSize: Float = 30f,
    val viewType: QuranViewType = QuranViewType.PAGE,
    val shouldStopOnSuraEnd: Boolean = false,
    val shouldStopOnPageEnd: Boolean = false,
    val shouldShowMenuTutorial: Boolean = true,
    val shouldShowReaderTutorial: Boolean = true,
    val werdPage: Int = 25,
    val isWerdDone: Boolean = false,
)