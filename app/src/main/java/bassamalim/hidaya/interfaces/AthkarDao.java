package bassamalim.hidaya.interfaces;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bassamalim.hidaya.models.AthkarDB;
import bassamalim.hidaya.models.JAyah;

@Dao
public interface AthkarDao {

    @Query("SELECT * FROM athkar")
    List<AthkarDB> getAll();

    @Query("SELECT * FROM athkar WHERE category_id = :category")
    List<AthkarDB> getList(int category);

    @Query("SELECT athkar_name FROM athkar WHERE category_id = :category AND athkar_id = :id")
    String getName(int category, int id);

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
