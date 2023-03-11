package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.TelawatRecitersDB

@Dao
interface TelawatRecitersDao {
    @Query("SELECT * FROM telawat_reciters")
    fun getAll(): List<TelawatRecitersDB>

    @Query("SELECT reciter_name FROM telawat_reciters")
    fun getNames(): List<String>

    @Query("SELECT reciter_name FROM telawat_reciters WHERE reciter_id == :id")
    fun getName(id: Int): String

    @Query("SELECT * FROM telawat_reciters WHERE favorite = 1")
    fun getFavorites(): List<TelawatRecitersDB>

    @Query("UPDATE telawat_reciters SET favorite = :value WHERE reciter_id = :id")
    fun setFav(id: Int, value: Int)

    @Query("SELECT favorite FROM telawat_reciters")
    fun getFavs(): List<Int>
}