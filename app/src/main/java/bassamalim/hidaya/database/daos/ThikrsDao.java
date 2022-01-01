package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.ThikrsDB;

@Dao
public interface ThikrsDao {

    @Query("SELECT * FROM thikrs WHERE athkar_id = :id")
    List<ThikrsDB> getThikrs(int id);

}
