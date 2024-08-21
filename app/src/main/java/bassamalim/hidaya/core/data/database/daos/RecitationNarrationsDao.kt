package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.RecitationNarrations

@Dao
interface RecitationNarrationsDao {

    @Query("SELECT * FROM recitation_narrations")
    fun getAll(): List<RecitationNarrations>

    @Query("SELECT * FROM recitation_narrations WHERE reciter_id = :reciterId")
    fun getReciterNarrations(reciterId: Int): List<RecitationNarrations>

    @Query(
        "SELECT * " +
        "FROM recitation_narrations " +
        "WHERE reciter_id = :reciterId AND id = :narrationId"
    )
    fun getNarration(reciterId: Int, narrationId: Int): RecitationNarrations

    @Query(
        "SELECT available_suras " +
        "FROM recitation_narrations " +
        "WHERE reciter_id = :reciterId AND id = :narrationId"
    )
    fun getAvailableSuras(reciterId: Int, narrationId: Int): String

}