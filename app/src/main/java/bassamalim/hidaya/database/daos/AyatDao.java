package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.AyatDB;

@Dao
public interface AyatDao {

    @Query("SELECT * FROM hafs_ayat")
    List<AyatDB> getAll();

    @Query("SELECT sura_name_ar FROM hafs_ayat")
    List<String> getNames();

    @Query("SELECT sura_name_en FROM hafs_ayat")
    List<String> getNamesEn();

}
