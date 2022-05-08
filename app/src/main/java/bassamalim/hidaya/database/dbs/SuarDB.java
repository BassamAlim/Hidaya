package bassamalim.hidaya.database.dbs;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "suar")
public class SuarDB {

    @PrimaryKey
    @ColumnInfo(name = "sura_id")
    private final int sura_id;
    @ColumnInfo(name = "sura_name")
    private final String sura_name;
    @ColumnInfo(name = "sura_name_en")
    private final String sura_name_en;
    @ColumnInfo(name = "search_name")
    private final String search_name;
    @ColumnInfo(name = "search_name_en")
    private final String search_name_en;
    @ColumnInfo(name = "tanzeel")
    private final int tanzeel;
    @ColumnInfo(name = "start_page")
    private final int start_page;
    @ColumnInfo(name = "favorite")
    private final int favorite;

    public SuarDB(int sura_id, String sura_name, String sura_name_en, String search_name,
                  String search_name_en, int tanzeel, int start_page, int favorite) {
        this.sura_id = sura_id;
        this.sura_name = sura_name;
        this.sura_name_en = sura_name_en;
        this.search_name = search_name;
        this.search_name_en = search_name_en;
        this.tanzeel = tanzeel;
        this.start_page = start_page;
        this.favorite = favorite;
    }

    public int getSura_id() {
        return sura_id;
    }

    public String getSura_name() {
        return sura_name;
    }

    public String getSura_name_en() {
        return sura_name_en;
    }

    public String getSearch_name() {
        return search_name;
    }

    public String getSearch_name_en() {
        return search_name_en;
    }

    public int getTanzeel() {
        return tanzeel;
    }

    public int getStart_page() {
        return start_page;
    }

    public int getFavorite() {
        return favorite;
    }
}
