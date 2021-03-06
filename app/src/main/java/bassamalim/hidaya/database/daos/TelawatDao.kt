package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.TelawatDB

@Dao
interface TelawatDao {
    @get:Query("SELECT * FROM telawat_reciters NATURAL JOIN telawat_versions")
    val all: List<TelawatDB>
}