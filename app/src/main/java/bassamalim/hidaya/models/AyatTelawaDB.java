package bassamalim.hidaya.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "ayat_telawa", primaryKeys = {"rec_id", "rate"},
        foreignKeys = @ForeignKey(entity = AyatRecitersDB.class,
                parentColumns = "rec_id", childColumns = "rec_id"))
public class AyatTelawaDB {

    @ColumnInfo(name = "rec_id")
    private final int rec_id;
    @ColumnInfo(name = "rate")
    private final int rate;
    @ColumnInfo(name = "source")
    private final String source;

    public AyatTelawaDB(int rec_id, int rate, String source) {
        this.rec_id = rec_id;
        this.rate = rate;
        this.source = source;
    }

    public int getRec_id() {
        return rec_id;
    }

    public int getRate() {
        return rate;
    }

    public String getSource() {
        return source;
    }
}
