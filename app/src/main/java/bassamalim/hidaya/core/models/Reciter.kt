package bassamalim.hidaya.core.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import java.io.Serializable

data class Reciter(
    val id: Int,
    val name: String,
    val versions: List<RecitationVersion>,
    var fav: MutableState<Int> = mutableIntStateOf(0)
) {
    data class RecitationVersion(
        val versionId: Int,
        val server: String,
        val rewaya: String,
        val count: Int,
        val suar: String
    ) : Serializable
}