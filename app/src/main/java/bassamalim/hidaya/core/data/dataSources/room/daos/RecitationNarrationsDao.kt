package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.dataSources.room.models.RecitationNarration

@Dao
interface RecitationNarrationsDao {

    @Query("SELECT * FROM recitation_narrations")
    fun getAll(): List<RecitationNarration>

    @Query("SELECT * FROM recitation_narrations WHERE reciter_id = :reciterId")
    fun getReciterNarrations(reciterId: Int): List<RecitationNarration>

    @Query(
        "SELECT * " +
        "FROM recitation_narrations " +
        "WHERE reciter_id = :reciterId AND id = :narrationId"
    )
    fun getNarration(reciterId: Int, narrationId: Int): RecitationNarration

    @Query(
        "SELECT available_suras " +
        "FROM recitation_narrations " +
        "WHERE reciter_id = :reciterId AND id = :narrationId"
    )
    fun getAvailableSuras(reciterId: Int, narrationId: Int): String

}