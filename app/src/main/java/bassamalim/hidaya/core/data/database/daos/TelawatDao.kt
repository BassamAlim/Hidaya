package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.TelawatDB

@Dao
interface TelawatDao {

    @get:Query(
        "SELECT " +
        "telawat_reciters.id AS reciter_id, telawat_reciters.name_ar AS reciter_name_ar, " +
        "telawat_reciters.name_en AS reciter_name_en, " +
        "telawat_rewayat.id AS rewayah_id, " +
        "telawat_rewayat.name_ar AS rewayah_name_ar, " +
        "telawat_rewayat.name_en AS rewayah_name_en, " +
        "telawat_rewayat.url AS rewayah_url, " +
        "telawat_rewayat.surah_total AS rewayah_surah_total, " +
        "telawat_rewayat.surah_list AS rewayah_surah_list " +
        "FROM telawat_reciters INNER JOIN telawat_rewayat " +
        "ON telawat_reciters.id = telawat_rewayat.reciter_id"
    )
    val all: List<TelawatDB>

    @Query(
        "SELECT " +
        "telawat_reciters.id AS reciter_id, telawat_reciters.name_ar AS reciter_name_ar, " +
        "telawat_reciters.name_en AS reciter_name_en, " +
        "telawat_rewayat.id AS rewayah_id, " +
        "telawat_rewayat.name_ar AS rewayah_name_ar, " +
        "telawat_rewayat.name_en AS rewayah_name_en, " +
        "telawat_rewayat.url AS rewayah_url, " +
        "telawat_rewayat.surah_total AS rewayah_surah_total, " +
        "telawat_rewayat.surah_list AS rewayah_surah_list " +
        "FROM telawat_reciters INNER JOIN telawat_rewayat " +
        "ON telawat_reciters.id = telawat_rewayat.reciter_id " +
        "WHERE telawat_reciters.id = :reciterId"
    )
    fun getReciterTelawat(reciterId: Int): List<TelawatDB>

}