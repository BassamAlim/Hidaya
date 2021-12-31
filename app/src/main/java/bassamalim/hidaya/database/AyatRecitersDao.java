package bassamalim.hidaya.database;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AyatRecitersDao {

    @Query("SELECT * FROM ayat_reciters")
    List<AyatRecitersDB> getAll();

    @Query("SELECT rec_name FROM ayat_reciters")
    List<String> getNames();

}
