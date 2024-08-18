package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.Verse

@Dao
interface VersesDao {

    @Query("SELECT * FROM verses")
    fun getAll(): List<Verse>

    @Query("SELECT page_num FROM verses WHERE id = :id")
    fun getVersePageNum(id: Int): Int

}