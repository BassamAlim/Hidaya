package bassamalim.hidaya.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ayat_reciters")
public class AyatRecitersDB {

    @PrimaryKey
    @ColumnInfo(name = "rec_id")
    private final int rec_id;
    @ColumnInfo(name = "rec_name")
    private final String rec_name;

    public AyatRecitersDB(int rec_id, String rec_name) {
        this.rec_id = rec_id;
        this.rec_name = rec_name;
    }

    public int getRec_id() {
        return rec_id;
    }

    public String getRec_name() {
        return rec_name;
    }
}
