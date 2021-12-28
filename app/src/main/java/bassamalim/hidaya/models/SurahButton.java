package bassamalim.hidaya.models;

import android.view.View;

import java.io.Serializable;

public class SurahButton implements Serializable {
    private final int number;
    private final String surahName;
    private final String searchName;
    private final int tanzeel;
    private int favorite;
    private final View.OnClickListener cardListener;

    public SurahButton(int number, String surahName, String searchName, int tanzeel,
                       int favorite, View.OnClickListener cardListener) {
        this.number = number;
        this.surahName = surahName;
        this.tanzeel = tanzeel;
        this.searchName = searchName;
        this.favorite = favorite;
        this.cardListener = cardListener;
    }

    public int getNumber() {
        return number;
    }

    public String getSurahName() {
        return surahName;
    }

    public String getSearchName() {
        return searchName;
    }

    public int getTanzeel() {
        return tanzeel;
    }

    public int getFavorite() {
        return favorite;
    }

    public View.OnClickListener getCardListener() {
        return cardListener;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}
