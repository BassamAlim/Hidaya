package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.models.City

@Dao
interface CityDao {

    @Query("SELECT * FROM cities WHERE country_id = :countryId AND name_ar LIKE '%' || :name || '%' LIMIT 50")
    fun getTopAr(countryId: Int, name: String): List<City>

    @Query("SELECT * FROM cities WHERE country_id = :countryId AND name_en LIKE '%' || :name || '%' LIMIT 50")
    fun getTopEn(countryId: Int, name: String): List<City>

    @Query("SELECT * FROM cities WHERE id = :id")
    fun getCity(id: Int): City

    @Query("SELECT *, MIN(ABS(latitude - :lat) + ABS(longitude - :lon)) FROM cities")
    fun getClosest(lat: Double, lon: Double): City

}