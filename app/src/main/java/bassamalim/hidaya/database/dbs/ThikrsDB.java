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
    @ColumnInfo(name = "title_en")
    private final String title_en;
    @ColumnInfo(name = "text")
    private final String text;
    @ColumnInfo(name = "text_en")
    private final String text_en;
    @ColumnInfo(name = "text_en_translation")
    private final String text_en_translation;
    @ColumnInfo(name = "repetition")
    private final String repetition;
    @ColumnInfo(name = "repetition_en")
    private final String repetition_en;
    @ColumnInfo(name = "fadl")
    private final String fadl;
    @ColumnInfo(name = "fadl_en")
    private final String fadl_en;
    @ColumnInfo(name = "reference")
    private final String reference;
    @ColumnInfo(name = "reference_en")
    private final String reference_en;
    @ColumnInfo(name = "athkar_id")
    private final int athkar_id;

    public ThikrsDB(int thikr_id, String title, String title_en, String text, String text_en,
                    String text_en_translation, String repetition, String repetition_en,
                    String fadl, String fadl_en, String reference,
                    String reference_en, int athkar_id) {

        this.thikr_id = thikr_id;
        this.title = title;
        this.title_en = title_en;
        this.text = text;
        this.text_en = text_en;
        this.text_en_translation = text_en_translation;
        this.repetition = repetition;
        this.repetition_en = repetition_en;
        this.fadl = fadl;
        this.fadl_en = fadl_en;
        this.reference = reference;
        this.reference_en = reference_en;
        this.athkar_id = athkar_id;
    }

    public int getThikr_id() {
        return thikr_id;
    }

    public String getTitle() {
        return title;
    }

    public String getTitle_en() {
        return title_en;
    }

    public String getText() {
        return text;
    }

    public String getText_en() {
        return text_en;
    }

    public String getText_en_translation() {
        return text_en_translation;
    }

    public String getRepetition() {
        return repetition;
    }

    public String getRepetition_en() {
        return repetition_en;
    }

    public String getFadl() {
        return fadl;
    }

    public String getFadl_en() {
        return fadl_en;
    }

    public String getReference() {
        return reference;
    }

    public String getReference_en() {
        return reference_en;
    }

    public int getAthkar_id() {
        return athkar_id;
    }
}
