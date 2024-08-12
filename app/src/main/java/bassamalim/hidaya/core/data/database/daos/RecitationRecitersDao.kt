package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.RecitationsReciter
import kotlinx.coroutines.flow.Flow

@Dao
interface RecitationRecitersDao {

    @Query("SELECT * FROM recitations_reciters")
    fun observeAll(): Flow<List<RecitationsReciter>>

    @Query("SELECT name_ar FROM recitations_reciters")
    fun getNamesAr(): List<String>

    @Query("SELECT name_en FROM recitations_reciters")
    fun getNamesEn(): List<String>

    @Query("SELECT name_ar FROM recitations_reciters WHERE id == :id")
    fun getNameAr(id: Int): String

    @Query("SELECT name_en FROM recitations_reciters WHERE id == :id")
    fun getNameEn(id: Int): String

    @Query("SELECT * FROM recitations_reciters WHERE is_favorite = 1")
    fun getFavorites(): List<RecitationsReciter>

    @Query("UPDATE recitations_reciters SET is_favorite = :value WHERE id = :id")
    suspend fun setIsFavorite(id: Int, value: Int)

    @Query("SELECT is_favorite FROM recitations_reciters")
    fun getIsFavorites(): List<Int>

}