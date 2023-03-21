package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.TelawatDB

@Dao
interface TelawatDao {
    @get:Query("SELECT * FROM telawat_reciters NATURAL JOIN telawat_versions")
    val all: List<TelawatDB>

    @Query("SELECT * FROM telawat_reciters NATURAL JOIN telawat_versions WHERE reciter_id = :reciterId")
    fun getReciterTelawat(reciterId: Int): List<TelawatDB>
}