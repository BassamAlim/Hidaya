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
        "recitation_versions.id AS version_id, " +
        "recitation_versions.name_ar AS version_name_ar, " +
        "recitation_versions.name_en AS version_name_en, " +
        "recitation_versions.url AS version_url, " +
        "recitation_versions.available_suras AS version_available_suras " +
        "FROM recitations_reciters INNER JOIN recitation_versions " +
        "ON recitations_reciters.id = recitation_versions.reciter_id"
    )
    fun getAll(): List<Recitation>

    @Query(
        "SELECT " +
        "recitations_reciters.id AS reciter_id, " +
        "recitations_reciters.name_ar AS reciter_name_ar, " +
        "recitations_reciters.name_en AS reciter_name_en, " +
        "recitation_versions.id AS version_id, " +
        "recitation_versions.name_ar AS version_name_ar, " +
        "recitation_versions.name_en AS version_name_en, " +
        "recitation_versions.url AS version_url, " +
        "recitation_versions.available_suras AS version_available_suras " +
        "FROM recitations_reciters INNER JOIN recitation_versions " +
        "ON recitations_reciters.id = recitation_versions.reciter_id " +
        "WHERE recitations_reciters.id = :reciterId"
    )
    fun getReciterTelawat(reciterId: Int): List<Recitation>

}