package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.room.models.VerseReciter

@Dao
interface VerseRecitersDao {

    @Query("SELECT * FROM verse_reciters")
    fun getAll(): List<VerseReciter>

    @Query("SELECT name FROM verse_reciters")
    fun getNames(): List<String>

}