package bassamalim.hidaya.database.dbs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "thikrs", primaryKeys = {"thikr_id", "athkar_id"},
        foreignKeys = @ForeignKey(entity = AthkarDB.class,
        parentColumns = "athkar_id", childColumns = "athkar_id",
        onUpdate = ForeignKey.CASCADE, onDelete = ForeignKey.SET_DEFAULT))
public class ThikrsDB {

    @ColumnInfo(name = "thikr_id")
    private final int thikr_id;
    @ColumnInfo(name = "title")
    private final String title;
    @ColumnInfo(name = "text")
    private final String text;
    @ColumnInfo(name = "repetition")
    private final String repetition;
    @ColumnInfo(name = "fadl")
    private final String fadl;
    @ColumnInfo(name = "reference")
    private final String reference;
    @ColumnInfo(name = "athkar_id")
    private final int athkar_id;

    public ThikrsDB(int thikr_id, String title, String text, String repetition, String fadl,
                    String reference, int athkar_id) {

        this.thikr_id = thikr_id;
        this.title = title;
        this.text = text;
        this.repetition = repetition;
        this.fadl = fadl;
        this.reference = reference;
        this.athkar_id = athkar_id;
    }

    public int getThikr_id() {
        return thikr_id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getRepetition() {
        return repetition;
    }

    public String getFadl() {
        return fadl;
    }

    public String getReference() {
        return reference;
    }

    public int getAthkar_id() {
        return athkar_id;
    }
}