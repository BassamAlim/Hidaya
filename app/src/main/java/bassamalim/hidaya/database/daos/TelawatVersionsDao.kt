package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.TelawatVersionsDB

@Dao
interface TelawatVersionsDao {
    @get:Query("SELECT * FROM telawat_versions")
    val all: List<TelawatVersionsDB>

    @Query(
        "SELECT * FROM telawat_versions " +
                "WHERE reciter_id = :reciter_id AND version_id = :version_id"
    )
    fun getVersion(reciter_id: Int, version_id: Int): TelawatVersionsDB

    @Query(
        "SELECT suras FROM telawat_versions " +
                "WHERE reciter_id = :reciter_id AND version_id = :version_id"
    )
    fun getSuras(reciter_id: Int, version_id: Int): String
}