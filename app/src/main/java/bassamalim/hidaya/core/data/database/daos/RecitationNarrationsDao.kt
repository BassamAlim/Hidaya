package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.RecitationsNarrations

@Dao
interface RecitationNarrationsDao {

    @Query("SELECT * FROM recitation_narrations")
    fun getAll(): List<RecitationsNarrations>

    @Query(
        "SELECT * " +
        "FROM recitation_narrations " +
        "WHERE reciter_id = :reciterId AND id = :narrationId"
    )
    fun getNarration(reciterId: Int, narrationId: Int): RecitationsNarrations

    @Query(
        "SELECT available_suras " +
        "FROM recitation_narrations " +
        "WHERE reciter_id = :reciterId AND id = :narrationId"
    )
    fun getAvailableSuras(reciterId: Int, narrationId: Int): String

}