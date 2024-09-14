package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.room.models.VerseRecitation

@Dao
interface VerseRecitationsDao {

    @Query("SELECT * FROM verse_recitations")
    fun getAll(): List<VerseRecitation>

    @Query("SELECT * FROM verse_recitations WHERE reciter_id = :reciterId")
    fun getReciterRecitations(reciterId: Int): List<VerseRecitation>

    @Query("SELECT COUNT(*) FROM verse_recitations")
    fun getSize(): Int

}