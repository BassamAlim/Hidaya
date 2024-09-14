package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.room.models.Sura
import kotlinx.coroutines.flow.Flow

@Dao
interface SurasDao {

    @Query("SELECT * FROM suras")
    fun observeAll(): Flow<List<Sura>>

    @Query("SELECT decorated_name_ar FROM suras")
    fun getDecoratedNamesAr(): List<String>

    @Query("SELECT decorated_name_en FROM suras")
    fun getDecoratedNamesEn(): List<String>

    @Query("SELECT plain_name_ar FROM suras")
    fun getPlainNamesAr(): List<String>

    @Query("SELECT decorated_name_ar FROM suras WHERE id == :id")
    fun getDecoratedNameAr(id: Int): String

    @Query("SELECT decorated_name_en FROM suras WHERE id == :id")
    fun getDecoratedNameEn(id: Int): String

    @Query("SELECT * FROM suras WHERE is_favorite = 1")
    fun observeFavorites(): Flow<List<Sura>>

    @Query("UPDATE suras SET is_favorite = :value WHERE id = :id")
    suspend fun setFavoriteStatus(id: Int, value: Int)

    @Query("SELECT is_favorite FROM suras")
    fun observeIsFavorites(): Flow<List<Int>>

    @Query("SELECT start_page FROM suras WHERE id = :id")
    fun getSuraStartPage(id: Int): Int

}