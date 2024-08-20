package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.Recitation

@Dao
interface RecitationsDao {

    @Query(
        "SELECT " +
        "recitations_reciters.id AS reciter_id, " +
        "recitations_reciters.name_ar AS reciter_name_ar, " +
        "recitations_reciters.name_en AS reciter_name_en, " +
        "recitation_narrations.id AS version_id, " +
        "recitation_narrations.name_ar AS version_name_ar, " +
        "recitation_narrations.name_en AS version_name_en, " +
        "recitation_narrations.url AS version_url, " +
        "recitation_narrations.available_suras AS version_available_suras " +
        "FROM recitations_reciters INNER JOIN recitation_narrations " +
        "ON recitations_reciters.id = recitation_narrations.reciter_id"
    )
    fun getAll(): List<Recitation>

    @Query(
        "SELECT " +
        "recitations_reciters.id AS reciter_id, " +
        "recitations_reciters.name_ar AS reciter_name_ar, " +
        "recitations_reciters.name_en AS reciter_name_en, " +
        "recitation_narrations.id AS version_id, " +
        "recitation_narrations.name_ar AS version_name_ar, " +
        "recitation_narrations.name_en AS version_name_en, " +
        "recitation_narrations.url AS version_url, " +
        "recitation_narrations.available_suras AS version_available_suras " +
        "FROM recitations_reciters INNER JOIN recitation_narrations " +
        "ON recitations_reciters.id = recitation_narrations.reciter_id " +
        "WHERE recitations_reciters.id = :reciterId"
    )
    fun getReciterRecitations(reciterId: Int): List<Recitation>

}