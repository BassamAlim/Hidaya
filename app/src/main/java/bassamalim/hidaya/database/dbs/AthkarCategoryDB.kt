package bassamalim.hidaya.database.dbs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athkar_categories")
class AthkarCategoryDB(
    @field:ColumnInfo(name = "category_id") @field:PrimaryKey private val categoryId: Int,
    @field:ColumnInfo(name = "category_name") private val categoryName: String?,
    @field:ColumnInfo(name = "category_name_en") private val categoryNameEn: String?
) {
    fun getCategoryId(): Int {
        return categoryId
    }

    fun getCategoryName(): String {
        return categoryName!!
    }

    fun getCategoryNameEn(): String {
        return categoryNameEn!!
    }
}