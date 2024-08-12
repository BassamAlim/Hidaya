package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.RemembranceCategory

@Dao
interface RemembranceCategoriesDao {

    @Query("SELECT * FROM remembrance_categories")
    fun getAll(): List<RemembranceCategory>

    @Query("SELECT name_ar FROM remembrance_categories WHERE id = :id")
    fun getNameAr(id: Int): String

    @Query("SELECT name_en FROM remembrance_categories WHERE id = :id")
    fun getNameEn(id: Int): String

}