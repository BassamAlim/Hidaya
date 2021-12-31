package bassamalim.hidaya.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TelawatDao {

    @Query("SELECT * FROM telawat_reciters NATURAL JOIN telawat_versions")
    List<TelawatDB> getAll();

}
