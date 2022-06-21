package bassamalim.hidaya.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athkar_categories")
class AthkarCategoryDB(
    @field:ColumnInfo(name = "category_id") @field:PrimaryKey private val category_id: Int,
    @field:ColumnInfo(name = "category_name") private val category_name: String?,
    @field:ColumnInfo(name = "category_name_en") private val category_name_en: String?
) {
    fun getCategoryId(): Int {
        return category_id
    }

    fun getCategoryName(): String {
        return category_name!!
    }

    fun getCategoryNameEn(): String {
        return category_name_en!!
    }
}