package bassamalim.hidaya.models

import java.io.Serializable

data class Reciter(
    val id: Int,
    val name: String,
    val versions: List<RecitationVersion>
) {

    data class RecitationVersion(
        private val versionId: Int,
        private val server: String,
        private val rewaya: String,
        private val count: Int,
        private val suras: String
    ) : Serializable {
        fun getVersionId(): Int {
            return versionId
        }

        fun getServer(): String {
            return server
        }

        fun getRewaya(): String {
            return rewaya
        }

        fun getCount(): Int {
            return count
        }

        fun getSuras(): String {
            return suras
        }
    }

}