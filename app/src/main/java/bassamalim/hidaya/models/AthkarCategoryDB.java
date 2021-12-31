package bassamalim.hidaya.models;

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

    public AthkarCategoryDB(int category_id, String category_name) {
        this.category_id = category_id;
        this.category_name = category_name;
    }

    public int getCategory_id() {
        return category_id;
    }

    public String getCategory_name() {
        return category_name;
    }
}
