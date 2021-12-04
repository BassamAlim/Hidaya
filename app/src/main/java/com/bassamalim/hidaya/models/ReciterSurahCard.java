package com.bassamalim.hidaya.models;

import android.view.View;

public class ReciterSurahCard {

    private final int num;
    private final String surahName;
    private final String searchName;
    private final View.OnClickListener listener;

    public ReciterSurahCard(int gNum, String gName, String gSearchName,
                            View.OnClickListener gListener) {
        num = gNum;
        surahName = gName;
        searchName = gSearchName;
        listener = gListener;
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
