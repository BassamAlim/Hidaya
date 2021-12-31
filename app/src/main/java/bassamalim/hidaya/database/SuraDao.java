package bassamalim.hidaya.database;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SuraDao {

    @Query("SELECT * FROM suar")
    List<SuraDB> getAll();

    @Query("SELECT sura_name FROM suar")
    List<String> getNames();

    @Query("SELECT favorite FROM suar")
    List<Integer> getFav();

    @Query("UPDATE suar SET favorite = :val WHERE sura_id = :index")
    void setFav(int index, int val);

    @Query("SELECT start_page FROM suar WHERE sura_id = :index")
    int getPage(int index);

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