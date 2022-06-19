package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.AthkarCategoryDB

@Dao
interface AthkarCategoryDao {
    @Query("SELECT * FROM athkar_categories")
    fun getAll(): List<AthkarCategoryDB>

    @Query("SELECT category_name FROM athkar_categories WHERE category_id = :id")
    fun getName(id: Int): String

    @Query("SELECT category_name_en FROM athkar_categories WHERE category_id = :id")
    fun getNameEn(id: Int): String
}