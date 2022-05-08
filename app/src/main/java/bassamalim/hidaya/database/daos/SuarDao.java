package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.SuarDB;

@Dao
public interface SuarDao {

    @Query("SELECT * FROM suar")
    List<SuarDB> getAll();

    @Query("SELECT sura_name FROM suar")
    List<String> getNames();

    @Query("SELECT sura_name_en FROM suar")
    List<String> getNamesEn();

    @Query("SELECT * FROM suar WHERE favorite = 1")
    List<SuarDB> getFavorites();

    @Query("UPDATE suar SET favorite = :val WHERE sura_id = :index")
    void setFav(int index, int val);

    @Query("SELECT favorite FROM suar")
    List<Integer> getFav();

    @Query("SELECT start_page FROM suar WHERE sura_id = :index")
    int getPage(int index);

}