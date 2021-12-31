package bassamalim.hidaya.database;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AyatDao {

    @Query("SELECT * FROM hafs_ayat")
    List<AyatDB> getAll();

/*
    @Query("SELECT * FROM hafs_ayat WHERE id IN (:userIds)")
    List<JAyah> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    JAyah findByName(String first, String last);

    @Insert
    void insertAll(JAyah... users);

    @Delete
    void delete(JAyah user);
    */
}
