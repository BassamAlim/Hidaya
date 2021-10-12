package com.bassamalim.athkar.models;

import android.text.SpannableString;
import android.widget.TextView;

public class Ayah {

    private final int juz;
    private final int surah;
    private final int ayah;
    private final String surahName;
    private String text;
    private final String tafseer;
    private int start;
    private int end;
    private SpannableString ss;
    private int index;
    private TextView screen;

    public Ayah(int gJuz, int gSurah, int gAyah, String gSurahName, String gText, String gTafseer) {
        juz = gJuz;
        surah = gSurah;
        ayah = gAyah;
        surahName = gSurahName;
        text = gText;
        tafseer = gTafseer;
    }

    public int getJuz() {
        return juz;
    }

    public int getSurah() {
        return surah;
    }

    public int getAyah() {
        return ayah;
    }

    public String getSurahName() {
        return surahName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getTafseer() {
        return tafseer;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public SpannableString getSS() {
        return ss;
    }

    public void setSS(SpannableString ss) {
        this.ss = ss;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public TextView getScreen() {
        return screen;
    }

    public void setScreen(TextView screen) {
        this.screen = screen;
    }
}
