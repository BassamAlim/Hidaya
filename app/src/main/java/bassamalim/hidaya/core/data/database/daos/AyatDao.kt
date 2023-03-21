package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.AyatDB

@Dao
interface AyatDao {
    @Query("SELECT * FROM ayat")
    fun getAll(): List<AyatDB>
}