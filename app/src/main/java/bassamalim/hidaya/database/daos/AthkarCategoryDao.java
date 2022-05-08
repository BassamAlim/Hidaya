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

    @Query("SELECT category_name_en FROM athkar_categories WHERE category_id = :id")
    String getNameEn(int id);

}
