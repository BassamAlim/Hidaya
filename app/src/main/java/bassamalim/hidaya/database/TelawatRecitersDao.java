package bassamalim.hidaya.database;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TelawatRecitersDao {

    @Query("SELECT * FROM telawat_reciters")
    List<TelawatRecitersDB> getAll();

    @Query("SELECT reciter_name FROM telawat_reciters")
    List<String> getNames();

}
