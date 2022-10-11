package bassamalim.hidaya.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "athkar",
    foreignKeys = [ForeignKey(
        entity = AthkarCategoryDB::class,
        parentColumns = arrayOf("category_id"),
        childColumns = arrayOf("category_id"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_DEFAULT
    )]
)
class AthkarDB(
    @field:ColumnInfo(name = "athkar_id") @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "athkar_name") val name: String?,
    @field:ColumnInfo(name = "athkar_name_en") val name_en: String?,
    @field:ColumnInfo(name = "category_id") val category_id: Int,
    @field:ColumnInfo(name = "favorite") var favorite: Int
)