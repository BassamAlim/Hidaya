package bassamalim.hidaya.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "thikrs", primaryKeys = {"thikr_id", "athkar_id", "category_id"},
        foreignKeys = @ForeignKey(entity = AthkarDB.class,
        parentColumns = {"athkar_id", "category_id"}, childColumns = {"athkar_id", "category_id"}))
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
    @ColumnInfo(name = "category_id")
    private final int category_id;

    public ThikrsDB(int thikr_id, String title, String text, String repetition, String fadl,
                    String reference, int athkar_id, int category_id) {

        this.thikr_id = thikr_id;
        this.title = title;
        this.text = text;
        this.repetition = repetition;
        this.fadl = fadl;
        this.reference = reference;
        this.athkar_id = athkar_id;
        this.category_id = category_id;
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

    public int getCategory_id() {
        return category_id;
    }
}
