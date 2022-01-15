package bassamalim.hidaya.database.dbs;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "telawat_versions", primaryKeys = {"reciter_id", "version_id"},
        foreignKeys = @ForeignKey(entity = TelawatRecitersDB.class,
                parentColumns = "reciter_id", childColumns = "reciter_id",
                onUpdate = ForeignKey.CASCADE, onDelete = ForeignKey.SET_DEFAULT))
public class TelawatVersionsDB {

    @ColumnInfo(name = "reciter_id")
    private final int reciter_id;
    @ColumnInfo(name = "version_id")
    private final int version_id;
    @ColumnInfo(name = "rewaya")
    private final String rewaya;
    @ColumnInfo(name = "url")
    private final String url;
    @ColumnInfo(name = "count")
    private final int count;
    @ColumnInfo(name = "suras")
    private final String suras;

    public TelawatVersionsDB(int reciter_id, int version_id, String rewaya, String url,
                             int count, String suras) {
        this.reciter_id = reciter_id;
        this.version_id = version_id;
        this.rewaya = rewaya;
        this.url = url;
        this.count = count;
        this.suras = suras;
    }

    public int getReciter_id() {
        return reciter_id;
    }

    public int getVersion_id() {
        return version_id;
    }

    @NonNull
    public String getRewaya() {
        return rewaya;
    }

    public String getUrl() {
        return url;
    }

    public int getCount() {
        return count;
    }

    public String getSuras() {
        return suras;
    }
}
