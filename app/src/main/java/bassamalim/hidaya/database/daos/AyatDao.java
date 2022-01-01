package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.AyatDB;

@Dao
public interface AyatDao {

    @Query("SELECT * FROM hafs_ayat")
    List<AyatDB> getAll();

/*
    @Insert
    void insertAll(JAyah... users);

    @Delete
    void delete(JAyah user);
*/
}
