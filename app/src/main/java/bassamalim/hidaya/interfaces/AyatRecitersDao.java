package bassamalim.hidaya.interfaces;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.models.AyatRecitersDB;

@Dao
public interface AyatRecitersDao {

    @Query("SELECT * FROM ayat_reciters")
    List<AyatRecitersDB> getAll();

    @Query("SELECT rec_name FROM ayat_reciters")
    List<String> getNames();

}
