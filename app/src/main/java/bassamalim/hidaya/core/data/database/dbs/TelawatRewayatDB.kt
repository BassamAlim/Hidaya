package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "telawat_rewayat",
    primaryKeys = ["reciter_id", "id"],
    foreignKeys = [ForeignKey(
        entity = TelawatRecitersDB::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("reciter_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
data class TelawatRewayatDB(
    @field:ColumnInfo(name = "id") val id: Int,
    @field:ColumnInfo(name = "reciter_id") val reciterId: Int,
    @field:ColumnInfo(name = "name_ar") val nameAr: String,
    @field:ColumnInfo(name = "name_en") val nameEn: String,
    @field:ColumnInfo(name = "url") val url: String,
    @field:ColumnInfo(name = "surah_total") val surahTotal: Int,
    @field:ColumnInfo(name = "surah_list") val surahList: String
)