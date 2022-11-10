package bassamalim.hidaya.models

import java.io.Serializable

data class Reciter(
    val id: Int,
    val name: String,
    val versions: List<RecitationVersion>
) {
    data class RecitationVersion(
        val versionId: Int,
        val server: String,
        val rewaya: String,
        val count: Int,
        val suras: String
    ) : Serializable
}