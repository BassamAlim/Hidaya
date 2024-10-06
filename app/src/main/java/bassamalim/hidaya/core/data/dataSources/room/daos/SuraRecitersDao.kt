package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import bassamalim.hidaya.core.data.dataSources.room.entities.SuraReciter
import kotlinx.coroutines.flow.Flow

@Dao
interface SuraRecitersDao {

    @Query("SELECT * FROM sura_reciters")
    fun observeAll(): Flow<List<SuraReciter>>

    @Query("SELECT * FROM sura_reciters WHERE is_favorite = 1")
    fun observeFavorites(): Flow<List<SuraReciter>>

    @Query("SELECT * FROM sura_reciters")
    fun getAll(): List<SuraReciter>

    @Query("SELECT * FROM sura_reciters WHERE id == :id")
    fun getReciter(id: Int): SuraReciter

    @Query("SELECT id, name_ar FROM sura_reciters")
    fun getNamesAr(): Map<@MapColumn("id") Int, @MapColumn("name_ar") String>

    @Query("SELECT id, name_en FROM sura_reciters")
    fun getNamesEn(): Map<@MapColumn("id") Int, @MapColumn("name_en") String>

    @Query("SELECT name_ar FROM sura_reciters WHERE id == :id")
    fun getNameAr(id: Int): String

    @Query("SELECT name_en FROM sura_reciters WHERE id == :id")
    fun getNameEn(id: Int): String

    @Query("SELECT id, is_favorite FROM sura_reciters")
    fun observeFavoriteStatuses():
            Flow<Map<@MapColumn("id") Int, @MapColumn("is_favorite") Int>>

    @Query("UPDATE sura_reciters SET is_favorite = :value WHERE id = :id")
    suspend fun setFavoriteStatus(id: Int, value: Int)

}