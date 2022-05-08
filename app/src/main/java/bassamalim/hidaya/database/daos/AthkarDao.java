package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.AthkarDB;

@Dao
public interface AthkarDao {

    @Query("SELECT * FROM athkar")
    List<AthkarDB> getAll();

    @Query("SELECT athkar_name FROM athkar")
    List<String> getNames();

    @Query("SELECT athkar_name_en FROM athkar")
    List<String> getNamesEn();

    @Query("SELECT * FROM athkar WHERE favorite = 1")
    List<AthkarDB> getFavorites();

    @Query("SELECT * FROM athkar WHERE category_id = :category")
    List<AthkarDB> getList(int category);

    @Query("SELECT athkar_name FROM athkar WHERE athkar_id = :id")
    String getName(int id);

    @Query("SELECT athkar_name_en FROM athkar WHERE athkar_id = :id")
    String getNameEn(int id);

    @Query("UPDATE athkar SET favorite = :val WHERE athkar_id = :id")
    void setFav(int id, int val);

    @Query("SELECT favorite FROM athkar")
    List<Integer> getFavs();

}
