package bassamalim.hidaya.database.daos;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.database.dbs.AyatTelawaDB;

@Dao
public interface AyatTelawaDao {

    @Query("SELECT * FROM ayat_telawa")
    List<AyatTelawaDB> getAll();

    @Query("SELECT * FROM ayat_telawa WHERE rec_id = :reciter")
    List<AyatTelawaDB> getReciter(int reciter);

}
