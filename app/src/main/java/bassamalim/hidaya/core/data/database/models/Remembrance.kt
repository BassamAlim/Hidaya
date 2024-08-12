package bassamalim.hidaya.core.data.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "remembrances",
    foreignKeys = [ForeignKey(
        entity = RemembranceCategory::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("category_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
data class Remembrance(
    @field:ColumnInfo(name = "id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "category_id") val category_id: Int,
    @field:ColumnInfo(name = "name_ar") val name_ar: String?,
    @field:ColumnInfo(name = "name_en") val name_en: String?,
    @field:ColumnInfo(name = "is_favorite") var is_favorite: Int
)