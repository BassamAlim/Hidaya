package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.TelawatVersionsDB

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
        "SELECT suar FROM telawat_versions " +
                "WHERE reciter_id = :reciter_id AND version_id = :version_id"
    )
    fun getSuar(reciter_id: Int, version_id: Int): String
}