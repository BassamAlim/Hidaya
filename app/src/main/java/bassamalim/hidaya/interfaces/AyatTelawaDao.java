package bassamalim.hidaya.interfaces;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import bassamalim.hidaya.models.AyatTelawaDB;
import bassamalim.hidaya.models.JAyah;

@Dao
public interface AyatTelawaDao {

    @Query("SELECT * FROM ayat_telawa")
    List<AyatTelawaDB> getAll();

    @Query("SELECT * FROM ayat_telawa WHERE rec_id = :reciter")
    List<AyatTelawaDB> getReciter(int reciter);

}
