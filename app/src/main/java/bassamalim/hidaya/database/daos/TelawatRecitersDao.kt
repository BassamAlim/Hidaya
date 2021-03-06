package bassamalim.hidaya.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.database.dbs.TelawatRecitersDB

@Dao
interface TelawatRecitersDao {
    @Query("SELECT * FROM telawat_reciters")
    fun getAll(): List<TelawatRecitersDB>

    @Query("SELECT reciter_name FROM telawat_reciters")
    fun getNames(): List<String>

    @Query("SELECT * FROM telawat_reciters WHERE favorite = 1")
    fun getFavorites(): List<TelawatRecitersDB>

    @Query("UPDATE telawat_reciters SET favorite = :val WHERE reciter_id = :id")
    fun setFav(id: Int, `val`: Int)

    @Query("SELECT favorite FROM telawat_reciters")
    fun getFavs(): List<Int>
}