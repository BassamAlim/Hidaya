package bassamalim.hidaya.core.models

import kotlinx.serialization.Serializable

@Serializable
data class QuranBookmarks(
    val bookmark1VerseId: Int? = null,
    val bookmark2VerseId: Int? = null,
    val bookmark3VerseId: Int? = null,
    val bookmark4VerseId: Int? = null
)
