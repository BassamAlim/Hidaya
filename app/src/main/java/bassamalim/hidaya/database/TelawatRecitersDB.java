package bassamalim.hidaya.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "telawat_reciters")
public class TelawatRecitersDB {

    @PrimaryKey
    @ColumnInfo(name = "reciter_id")
    private final int reciter_id;
    @ColumnInfo(name = "reciter_name")
    private final String reciter_name;

    public TelawatRecitersDB(int reciter_id, String reciter_name) {
        this.reciter_id = reciter_id;
        this.reciter_name = reciter_name;
    }

    public int getReciter_id() {
        return reciter_id;
    }

    public String getReciter_name() {
        return reciter_name;
    }
}
