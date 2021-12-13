package bassamalim.hidaya.models;

import android.view.View;

public class SurahButton {
    private final int number;
    private final String surahName;
    private final String searchName;
    private final String tanzeel;
    private final View.OnClickListener listener;

    public SurahButton(int number, String surahName, String searchName, String tanzeel,
                       View.OnClickListener listener) {
        this.number = number;
        this.surahName = surahName;
        this.tanzeel = tanzeel;
        this.searchName = searchName;
        this.listener = listener;
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

    public String getTanzeel() {
        return tanzeel;
    }

    public View.OnClickListener getListener() {
        return listener;
    }

}
