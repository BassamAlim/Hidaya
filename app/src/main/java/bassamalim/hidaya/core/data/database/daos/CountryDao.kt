package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.CountryDB

@Dao
interface CountryDao {

    @Query("SELECT * FROM countries")
    fun getAll(): List<CountryDB>

    @Query("SELECT name_ar FROM countries WHERE id = :id")
    fun getNameAr(id: Int): String

    @Query("SELECT name_en FROM countries WHERE id = :id")
    fun getNameEn(id: Int): String

}