package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.TelawatRecitersDB;

@Dao
public interface TelawatRecitersDao {

    @Query("SELECT * FROM telawat_reciters")
    List<TelawatRecitersDB> getAll();

    @Query("SELECT reciter_name FROM telawat_reciters")
    List<String> getNames();

    @Query("SELECT * FROM telawat_reciters WHERE favorite = 1")
    List<TelawatRecitersDB> getFavorites();

    @Query("UPDATE telawat_reciters SET favorite = :val WHERE reciter_id = :id")
    void setFav(int id, int val);

    @Query("SELECT favorite FROM telawat_reciters")
    List<Integer> getFavs();

}
