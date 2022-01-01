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
    @ColumnInfo(name = "sura_name_en")
    private final String sura_name_en;
    @ColumnInfo(name = "sura_name_ar")
    private final String sura_name_ar;
    @ColumnInfo(name = "page")
    private final int page;
    @ColumnInfo(name = "aya_no")
    private final int aya_no;
    @ColumnInfo(name = "aya_text")
    private final String aya_text;
    @ColumnInfo(name = "aya_text_emlaey")
    private final String aya_text_emlaey;
    @ColumnInfo(name = "aya_tafseer")
    private final String aya_tafseer;

    public AyatDB(int id, int jozz, int sura_no, String sura_name_en, String sura_name_ar, int page,
                  int aya_no, String aya_text, String aya_text_emlaey, String aya_tafseer) {
        this.id = id;
        this.jozz = jozz;
        this.sura_no = sura_no;
        this.sura_name_en = sura_name_en;
        this.sura_name_ar = sura_name_ar;
        this.page = page;
        this.aya_no = aya_no;
        this.aya_text = aya_text;
        this.aya_text_emlaey = aya_text_emlaey;
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

    public String getSura_name_en() {
        return sura_name_en;
    }

    public String getSura_name_ar() {
        return sura_name_ar;
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

    public String getAya_text_emlaey() {
        return aya_text_emlaey;
    }

    public String getAya_tafseer() {
        return aya_tafseer;
    }
}