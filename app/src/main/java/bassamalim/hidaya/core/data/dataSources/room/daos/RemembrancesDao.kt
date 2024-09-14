package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.room.models.Remembrance
import kotlinx.coroutines.flow.Flow

@Dao
interface RemembrancesDao {

    @Query("SELECT * FROM remembrances")
    fun observeAll(): Flow<List<Remembrance>>

    @Query("SELECT * FROM remembrances WHERE is_favorite = 1")
    fun observeFavorites(): Flow<List<Remembrance>>

    @Query("SELECT is_favorite FROM remembrances")
    fun observeFavoriteStatuses(): Flow<List<Int>>

    @Query("SELECT * FROM remembrances WHERE category_id = :categoryId")
    fun observeCategoryRemembrances(categoryId: Int): Flow<List<Remembrance>>

    @Query("SELECT name_ar FROM remembrances WHERE id = :id")
    fun getNameAr(id: Int): String

    @Query("SELECT name_en FROM remembrances WHERE id = :id")
    fun getNameEn(id: Int): String

    @Query("UPDATE remembrances SET is_favorite = :value WHERE id = :id")
    suspend fun setFavoriteStatus(id: Int, value: Int)

}