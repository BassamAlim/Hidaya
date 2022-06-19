package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.AyatRecitersDB

@Dao
interface AyatRecitersDao {
    @Query("SELECT * FROM ayat_reciters")
    fun getAll(): List<AyatRecitersDB>

    @Query("SELECT rec_name FROM ayat_reciters")
    fun getNames(): List<String>
}