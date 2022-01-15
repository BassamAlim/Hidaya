package bassamalim.hidaya.models;

import android.view.View;

public class ReciterSurahCard {

    private final int num;
    private final String surahName;
    private final String searchName;
    private final View.OnClickListener listener;

    public ReciterSurahCard(int num, String name, String searchName,
                            View.OnClickListener listener) {
        this.num = num;
        this.surahName = name;
        this.searchName = searchName;
        this.listener = listener;
    }

    public String getSurahName() {
        return surahName;
    }

    public int getNum() {
        return num;
    }

    public String getSearchName() {
        return searchName;
    }

    public View.OnClickListener getListener() {
        return listener;
    }
}
