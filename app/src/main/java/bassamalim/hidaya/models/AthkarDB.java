package bassamalim.hidaya.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "athkar", primaryKeys = {"athkar_id", "category_id"},
        foreignKeys = @ForeignKey(entity = AthkarCategoryDB.class,
        parentColumns = "category_id", childColumns = "category_id"))
public class AthkarDB {

    @ColumnInfo(name = "athkar_id")
    private final int athkar_id;
    @ColumnInfo(name = "athkar_name")
    private final String athkar_name;
    @ColumnInfo(name = "category_id")
    private final int category_id;

    public AthkarDB(int athkar_id, String athkar_name, int category_id) {
        this.athkar_id = athkar_id;
        this.athkar_name = athkar_name;
        this.category_id = category_id;
    }

    public int getAthkar_id() {
        return athkar_id;
    }

    public String getAthkar_name() {
        return athkar_name;
    }

    public int getCategory_id() {
        return category_id;
    }
}
