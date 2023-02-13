package bassamalim.hidaya.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
class CountryDB(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "name_ar") val nameAr: String,
    @field:ColumnInfo(name = "name_en") val nameEn: String
)