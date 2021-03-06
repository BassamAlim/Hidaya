package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.SuarDB

@Dao
interface SuarDao {
    @Query("SELECT * FROM suar")
    fun getAll(): List<SuarDB>

    @Query("SELECT sura_name FROM suar")
    fun getNames(): List<String>

    @Query("SELECT sura_name_en FROM suar")
    fun getNamesEn(): List<String>

    @Query("SELECT * FROM suar WHERE favorite = 1")
    fun getFavorites(): List<SuarDB>

    @Query("UPDATE suar SET favorite = :val WHERE sura_id = :index")
    fun setFav(index: Int, `val`: Int)

    @Query("SELECT favorite FROM suar")
    fun getFav(): List<Int>

    @Query("SELECT start_page FROM suar WHERE sura_id = :index")
    fun getPage(index: Int): Int
}