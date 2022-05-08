package bassamalim.hidaya.database.dbs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "athkar_categories")
public class AthkarCategoryDB {

    @PrimaryKey
    @ColumnInfo(name = "category_id")
    private final int category_id;
    @ColumnInfo(name = "category_name")
    private final String category_name;
    @ColumnInfo(name = "category_name_en")
    private final String category_name_en;

    public AthkarCategoryDB(int category_id, String category_name, String category_name_en) {
        this.category_id = category_id;
        this.category_name = category_name;
        this.category_name_en = category_name_en;
    }

    public int getCategory_id() {
        return category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public String getCategory_name_en() {
        return category_name_en;
    }
}
