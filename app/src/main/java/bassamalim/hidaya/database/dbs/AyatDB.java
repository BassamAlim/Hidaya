package bassamalim.hidaya.database.dbs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hafs_ayat")
public class AyatDB {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private final int id;
    @ColumnInfo(name = "jozz")
    private final int jozz;
    @ColumnInfo(name = "sura_no")
    private final int sura_no;
    @ColumnInfo(name = "page")
    private final int page;
    @ColumnInfo(name = "aya_no")
    private final int aya_no;
    @ColumnInfo(name = "aya_text")
    private final String aya_text;
    @ColumnInfo(name = "aya_text_en")
    private final String aya_text_en;
    @ColumnInfo(name = "aya_text_emlaey")
    private final String aya_text_emlaey;
    @ColumnInfo(name = "aya_translation_en")
    private final String aya_translation_en;
    @ColumnInfo(name = "aya_tafseer")
    private final String aya_tafseer;

    public AyatDB(int id, int jozz, int sura_no, int page,
                  int aya_no, String aya_text, String aya_text_en, String aya_text_emlaey,
                  String aya_translation_en, String aya_tafseer) {
        this.id = id;
        this.jozz = jozz;
        this.sura_no = sura_no;
        this.page = page;
        this.aya_no = aya_no;
        this.aya_text = aya_text;
        this.aya_text_en = aya_text_en;
        this.aya_text_emlaey = aya_text_emlaey;
        this.aya_translation_en = aya_translation_en;
        this.aya_tafseer = aya_tafseer;
    }

    public int getId() {
        return id;
    }

    public int getJozz() {
        return jozz;
    }

    public int getSura_no() {
        return sura_no;
    }

    public int getPage() {
        return page;
    }

    public int getAya_no() {
        return aya_no;
    }

    public String getAya_text() {
        return aya_text;
    }

    public String getAya_text_en() {
        return aya_text_en;
    }

    public String getAya_text_emlaey() {
        return aya_text_emlaey;
    }

    public String getAya_translation_en() {
        return aya_translation_en;
    }

    public String getAya_tafseer() {
        return aya_tafseer;
    }
}