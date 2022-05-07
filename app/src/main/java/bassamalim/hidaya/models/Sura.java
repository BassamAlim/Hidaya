package bassamalim.hidaya.models;

import android.view.View;

import java.io.Serializable;

public class Sura implements Serializable {
    private final int number;
    private final String suraName;
    private final String searchName;
    private final int tanzeel;
    private int favorite;
    private final View.OnClickListener cardListener;

    public Sura(int number, String suraName, String searchName, int tanzeel,
                int favorite, View.OnClickListener cardListener) {
        this.number = number;
        this.suraName = suraName;
        this.tanzeel = tanzeel;
        this.searchName = searchName;
        this.favorite = favorite;
        this.cardListener = cardListener;
    }

    public int getNumber() {
        return number;
    }

    public String getSuraName() {
        return suraName;
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
