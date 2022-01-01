package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.TelawatDB;

@Dao
public interface TelawatDao {

    @Query("SELECT * FROM telawat_reciters NATURAL JOIN telawat_versions")
    List<TelawatDB> getAll();

}
