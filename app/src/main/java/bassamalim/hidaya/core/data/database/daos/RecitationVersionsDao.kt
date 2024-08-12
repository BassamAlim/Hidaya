package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.RecitationsVersion

@Dao
interface RecitationVersionsDao {

    @Query("SELECT * FROM recitation_versions")
    fun getAll(): List<RecitationsVersion>

    @Query(
        "SELECT * " +
        "FROM recitation_versions " +
        "WHERE reciter_id = :reciterId AND id = :versionId"
    )
    fun getVersion(reciterId: Int, versionId: Int): RecitationsVersion

    @Query(
        "SELECT available_suras " +
        "FROM recitation_versions " +
        "WHERE reciter_id = :reciterId AND id = :versionId"
    )
    fun getAvailableSuras(reciterId: Int, versionId: Int): String

}