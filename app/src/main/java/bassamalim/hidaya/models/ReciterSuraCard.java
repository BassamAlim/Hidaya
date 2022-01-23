package bassamalim.hidaya.models;

import android.view.View;

public class ReciterSuraCard {

    private final int num;
    private final String surahName;
    private final String searchName;
    private int favorite;
    private final View.OnClickListener listener;

    public ReciterSuraCard(int num, String name, String searchName, int favorite,
                           View.OnClickListener listener) {
        this.num = num;
        this.surahName = name;
        this.searchName = searchName;
        this.favorite = favorite;
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

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getFavorite() {
        return favorite;
    }

    public View.OnClickListener getListener() {
        return listener;
    }
}
