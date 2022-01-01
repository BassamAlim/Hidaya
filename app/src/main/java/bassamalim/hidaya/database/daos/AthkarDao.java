package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.AthkarDB;

@Dao
public interface AthkarDao {

    @Query("SELECT * FROM athkar")
    List<AthkarDB> getAll();

    @Query("SELECT * FROM athkar WHERE favorite = 1")
    List<AthkarDB> getFavorites();

    @Query("SELECT * FROM athkar WHERE category_id = :category")
    List<AthkarDB> getList(int category);

    @Query("SELECT athkar_name FROM athkar WHERE athkar_id = :id")
    String getName(int id);

    @Query("UPDATE athkar SET favorite = :val WHERE athkar_id = :id")
    void setFav(int id, int val);

    @Query("SELECT favorite FROM athkar")
    List<Integer> getFavs();

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
