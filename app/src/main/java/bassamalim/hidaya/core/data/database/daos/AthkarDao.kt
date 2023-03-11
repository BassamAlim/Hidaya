package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.AthkarDB

@Dao
interface AthkarDao {
    @Query("SELECT * FROM athkar")
    fun getAll(): List<AthkarDB>

    @Query("SELECT athkar_name FROM athkar")
    fun getNames(): List<String>

    @Query("SELECT athkar_name_en FROM athkar")
    fun getNamesEn(): List<String>

    @Query("SELECT * FROM athkar WHERE favorite = 1")
    fun getFavorites(): List<AthkarDB>

    @Query("SELECT * FROM athkar WHERE category_id = :category")
    fun getList(category: Int): List<AthkarDB>

    @Query("SELECT athkar_name FROM athkar WHERE athkar_id = :id")
    fun getName(id: Int): String

    @Query("SELECT athkar_name_en FROM athkar WHERE athkar_id = :id")
    fun getNameEn(id: Int): String

    @Query("UPDATE athkar SET favorite = :value WHERE athkar_id = :id")
    fun setFav(id: Int, value: Int)

    @Query("SELECT favorite FROM athkar")
    fun getFavs(): List<Int>
}