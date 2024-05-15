package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.TelawatRecitersDB

@Dao
interface TelawatRecitersDao {
    @Query("SELECT * FROM telawat_reciters")
    fun getAll(): List<TelawatRecitersDB>

    @Query("SELECT name_ar FROM telawat_reciters")
    fun getNamesAr(): List<String>

    @Query("SELECT name_en FROM telawat_reciters")
    fun getNamesEn(): List<String>

    @Query("SELECT name_ar FROM telawat_reciters WHERE id == :id")
    fun getNameAr(id: Int): String

    @Query("SELECT name_en FROM telawat_reciters WHERE id == :id")
    fun getNameEn(id: Int): String

    @Query("SELECT * FROM telawat_reciters WHERE favorite = 1")
    fun getFavorites(): List<TelawatRecitersDB>

    @Query("UPDATE telawat_reciters SET favorite = :value WHERE id = :id")
    fun setFav(id: Int, value: Int)

    @Query("SELECT favorite FROM telawat_reciters")
    fun getFavs(): List<Int>
}