package bassamalim.hidaya.core.data.database.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.database.dbs.CityDB

@Dao
interface CityDao {

    @Query("SELECT * FROM cities WHERE country_id = :countryId AND name_ar LIKE '%' || :name || '%' LIMIT 50")
    fun getTopAr(countryId: Int, name: String): List<CityDB>

    @Query("SELECT * FROM cities WHERE country_id = :countryId AND name_en LIKE '%' || :name || '%' LIMIT 50")
    fun getTopEn(countryId: Int, name: String): List<CityDB>

    @Query("SELECT * FROM cities WHERE id = :id")
    fun getCity(id: Int): CityDB

    @Query("SELECT *, MIN(ABS(latitude - :lat) + ABS(longitude - :lon)) FROM cities")
    fun getClosest(lat: Double, lon: Double): CityDB

    /*@Query("SELECT *, MIN(POWER(POWER(ABS(latitude - :gLat), 2.0) + POWER(ABS(longitude - :gLng), 2.0), 0.5)) FROM cities")
    fun getClosest(gLat: Double, gLng: Double): CityDB*/
}