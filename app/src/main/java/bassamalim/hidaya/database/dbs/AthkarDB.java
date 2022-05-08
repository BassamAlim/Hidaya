package bassamalim.hidaya.database.dbs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "athkar", foreignKeys = @ForeignKey(entity = AthkarCategoryDB.class,
        parentColumns = "category_id", childColumns = "category_id",
                onUpdate = ForeignKey.CASCADE, onDelete = ForeignKey.SET_DEFAULT))
public class AthkarDB {

    @PrimaryKey
    @ColumnInfo(name = "athkar_id")
    private final int athkar_id;
    @ColumnInfo(name = "athkar_name")
    private final String athkar_name;
    @ColumnInfo(name = "athkar_name_en")
    private final String athkar_name_en;
    @ColumnInfo(name = "category_id")
    private final int category_id;
    @ColumnInfo(name = "favorite")
    private final int favorite;

    public AthkarDB(int athkar_id, String athkar_name, String athkar_name_en,
                    int category_id, int favorite) {
        this.athkar_id = athkar_id;
        this.athkar_name = athkar_name;
        this.athkar_name_en = athkar_name_en;
        this.category_id = category_id;
        this.favorite = favorite;
    }

    public int getAthkar_id() {
        return athkar_id;
    }

    public String getAthkar_name() {
        return athkar_name;
    }

    public String getAthkar_name_en() {
        return athkar_name_en;
    }

    public int getCategory_id() {
        return category_id;
    }

    public int getFavorite() {
        return favorite;
    }
}
