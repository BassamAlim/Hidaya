package com.bassamalim.athkar.models;

import android.content.Context;

public class SurahButton extends androidx.appcompat.widget.AppCompatButton {
    private int number;
    private String surahName;

    public SurahButton(Context context, String surahName) {
        super(context);
        this.surahName = surahName;
    }

    public int getNumber() {
        return number;
    }

    public String getSurahName() {
        return surahName;
    }
}
