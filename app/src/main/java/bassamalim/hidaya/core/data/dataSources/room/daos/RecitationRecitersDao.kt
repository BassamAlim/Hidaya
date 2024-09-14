package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import bassamalim.hidaya.core.data.dataSources.room.models.RecitationsReciter
import kotlinx.coroutines.flow.Flow

@Dao
interface RecitationRecitersDao {

    @Query("SELECT * FROM recitations_reciters")
    fun observeAll(): Flow<List<RecitationsReciter>>

    @Query("SELECT * FROM recitations_reciters WHERE is_favorite = 1")
    fun observeFavorites(): Flow<List<RecitationsReciter>>

    @Query("SELECT * FROM recitations_reciters")
    fun getAll(): List<RecitationsReciter>

    @Query("SELECT * FROM recitations_reciters WHERE id == :id")
    fun getReciter(id: Int): RecitationsReciter

    @Query("SELECT id, name_ar FROM recitations_reciters")
    fun getNamesAr(): Map<@MapColumn("id") Int, @MapColumn("name_ar") String>

    @Query("SELECT id, name_en FROM recitations_reciters")
    fun getNamesEn(): Map<@MapColumn("id") Int, @MapColumn("name_en") String>

    @Query("SELECT name_ar FROM recitations_reciters WHERE id == :id")
    fun getNameAr(id: Int): String

    @Query("SELECT name_en FROM recitations_reciters WHERE id == :id")
    fun getNameEn(id: Int): String

    @Query("SELECT id, is_favorite FROM recitations_reciters")
    fun observeFavoriteStatuses():
            Flow<Map<@MapColumn("id") Int, @MapColumn("is_favorite") Int>>

    @Query("UPDATE recitations_reciters SET is_favorite = :value WHERE id = :id")
    suspend fun setFavoriteStatus(id: Int, value: Int)

}