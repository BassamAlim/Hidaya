package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.TelawatVersionsDB;

@Dao
public interface TelawatVersionsDao {

    @Query("SELECT * FROM telawat_versions")
    List<TelawatVersionsDB> getAll();

    @Query("SELECT * FROM telawat_versions WHERE reciter_id = :id AND rewaya = :rewaya")
    TelawatVersionsDB getVersion(int id, String rewaya);

    @Query("SELECT suras FROM telawat_versions WHERE reciter_id = :id AND rewaya = :rewaya")
    String getSuras(int id, String rewaya);

}
