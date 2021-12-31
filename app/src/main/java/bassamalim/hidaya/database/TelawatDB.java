package bassamalim.hidaya.database;

import androidx.room.Embedded;

public class TelawatDB {

    /*@Embedded
    TelawatRecitersDB reciters;

    @Embedded
    TelawatVersionsDB versions;*/

    private int reciter_id;
    private String reciter_name;
    private String rewaya;
    private String url;
    private int count;
    private String suras;

    public TelawatDB() {}

    public TelawatDB(TelawatDB in) {
        reciter_id = in.reciter_id;
        reciter_name = in.reciter_name;
        rewaya = in.rewaya;
        url = in.url;
        count = in.count;
        suras = in.suras;
    }

    public void setReciter_id(int reciter_id) {
        this.reciter_id = reciter_id;
    }

    public void setReciter_name(String reciter_name) {
        this.reciter_name = reciter_name;
    }

    public void setRewaya(String rewaya) {
        this.rewaya = rewaya;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setSuras(String suras) {
        this.suras = suras;
    }

    public int getReciter_id() {
        return reciter_id;
    }

    public String getReciter_name() {
        return reciter_name;
    }

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
