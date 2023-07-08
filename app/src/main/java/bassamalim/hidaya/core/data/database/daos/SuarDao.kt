package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.SuarDB

@Dao
interface SuarDao {
    @Query("SELECT * FROM suar")
    fun getAll(): List<SuarDB>

    @Query("SELECT name FROM suar")
    fun getNames(): List<String>

    @Query("SELECT name FROM suar")
    fun getSearchNames(): List<String>

    @Query("SELECT name_en FROM suar")
    fun getNamesEn(): List<String>

    @Query("SELECT name FROM suar WHERE id == :id")
    fun getName(id: Int): String

    @Query("SELECT name_en FROM suar WHERE id == :id")
    fun getNameEn(id: Int): String

    @Query("SELECT * FROM suar WHERE favorite = 1")
    fun getFavorites(): List<SuarDB>

    @Query("UPDATE suar SET favorite = :value WHERE id = :index")
    fun setFav(index: Int, value: Int)

    @Query("SELECT favorite FROM suar")
    fun getFavs(): List<Int>

    @Query("SELECT start_page FROM suar WHERE id = :index")
    fun getPage(index: Int): Int
}