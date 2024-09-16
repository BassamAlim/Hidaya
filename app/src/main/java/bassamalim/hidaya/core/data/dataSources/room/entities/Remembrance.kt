package bassamalim.hidaya.core.data.dataSources.room.entities

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
    @field:ColumnInfo(name = "category_id") val categoryId: Int,
    @field:ColumnInfo(name = "name_ar") val nameAr: String?,
    @field:ColumnInfo(name = "name_en") val nameEn: String?,
    @field:ColumnInfo(name = "is_favorite") var isFavorite: Int
)