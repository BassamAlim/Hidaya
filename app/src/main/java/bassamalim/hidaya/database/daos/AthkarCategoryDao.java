package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.AthkarCategoryDB;

@Dao
public interface AthkarCategoryDao {

    @Query("SELECT * FROM athkar_categories")
    List<AthkarCategoryDB> getAll();

    @Query("SELECT category_name FROM athkar_categories WHERE category_id = :id")
    String getName(int id);

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