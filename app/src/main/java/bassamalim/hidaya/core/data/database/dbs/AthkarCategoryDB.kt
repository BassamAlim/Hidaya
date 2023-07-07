package bassamalim.hidaya.core.data.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athkar_categories")
data class AthkarCategoryDB(
    @field:ColumnInfo(name = "category_id") @field:PrimaryKey val categoryId: Int,
    @field:ColumnInfo(name = "category_name") val categoryName: String?,
    @field:ColumnInfo(name = "category_name_en") val categoryNameEn: String?
)