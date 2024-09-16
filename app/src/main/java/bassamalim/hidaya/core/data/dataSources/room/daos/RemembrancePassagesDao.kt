package bassamalim.hidaya.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Query
import bassamalim.hidaya.core.data.dataSources.room.entities.RemembrancePassage

@Dao
interface RemembrancePassagesDao {

    @Query("SELECT * FROM remembrance_passages WHERE remembrance_id = :remembranceId Order By id")
    fun getRemembrancePassages(remembranceId: Int): List<RemembrancePassage>

}