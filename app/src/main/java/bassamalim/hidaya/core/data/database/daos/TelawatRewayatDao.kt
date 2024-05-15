package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.TelawatRewayatDB

@Dao
interface TelawatRewayatDao {
    @get:Query("SELECT * FROM telawat_rewayat")
    val all: List<TelawatRewayatDB>

    @Query(
        "SELECT * " +
        "FROM telawat_rewayat " +
        "WHERE reciter_id = :reciterId AND id = :versionId"
    )
    fun getVersion(reciterId: Int, versionId: Int): TelawatRewayatDB

    @Query(
        "SELECT surah_list " +
        "FROM telawat_rewayat " +
        "WHERE reciter_id = :reciterId AND id = :versionId"
    )
    fun getSuar(reciterId: Int, versionId: Int): String
}