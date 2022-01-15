package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.TelawatVersionsDB;

@Dao
public interface TelawatVersionsDao {

    @Query("SELECT * FROM telawat_versions")
    List<TelawatVersionsDB> getAll();

    @Query("SELECT * FROM telawat_versions " +
            "WHERE reciter_id = :reciter_id AND version_id = :version_id")
    TelawatVersionsDB getVersion(int reciter_id, int version_id);

    @Query("SELECT suras FROM telawat_versions " +
            "WHERE reciter_id = :reciter_id AND version_id = :version_id")
    String getSuras(int reciter_id, int version_id);

}
