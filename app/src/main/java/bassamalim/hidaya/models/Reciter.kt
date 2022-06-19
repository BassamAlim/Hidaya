package bassamalim.hidaya.models

import android.view.View
import java.io.Serializable

data class Reciter(
    private val id: Int,
    private val name: String,
    private var favorite: Int,
    private val versions: List<RecitationVersion>
) {

    fun getId(): Int {
        return id
    }

    fun getName(): String {
        return name
    }

    fun setFavorite(favorite: Int) {
        this.favorite = favorite
    }

    fun getFavorite(): Int {
        return favorite
    }

    fun getVersions(): List<RecitationVersion> {
        return versions
    }

    class RecitationVersion(
        private val versionId: Int,
        private val server: String,
        private val rewaya: String,
        private val count: Int,
        private val suras: String,
        private val listener: View.OnClickListener?
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

        fun getListener(): View.OnClickListener {
            return listener!!
        }
    }
}