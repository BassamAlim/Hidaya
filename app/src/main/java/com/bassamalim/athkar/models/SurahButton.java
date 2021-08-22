package com.bassamalim.athkar.models;

import android.view.View;

public class SurahButton {
    private final int number;
    private final String surahName;
    private final View.OnClickListener listener;

    public SurahButton(int number, String surahName, View.OnClickListener listener) {
        this.number = number;
        this.surahName = surahName;
        this.listener = listener;
    }

    public int getNumber() {
        return number;
    }

    public String getSurahName() {
        return surahName;
    }

    public View.OnClickListener getListener() {
        return listener;
    }

}
