package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.AthkarPartsDB

@Dao
interface AthkarPartsDao {

    @Query("SELECT * FROM athkar_parts WHERE athkar_id = :id Order By part_id")
    fun getThikrParts(id: Int): List<AthkarPartsDB>

}