package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.AyatTelawaDB

@Dao
interface AyatTelawaDao {
    @get:Query("SELECT * FROM ayat_telawa")
    val all: List<AyatTelawaDB>

    @Query("SELECT * FROM ayat_telawa WHERE rec_id = :reciter")
    fun getReciter(reciter: Int): List<AyatTelawaDB>

    @get:Query("SELECT COUNT(*) FROM ayat_telawa")
    val size: Int
}