package com.bassamalim.athkar.models;

public class Ayah {

    private final int juz;
    private final String surahName;
    private String text;
    private final String tafseer;
    private int start;
    private int end;

    public Ayah(int gJuz, String gSurahName, String gText, String gTafseer) {
        juz = gJuz;
        surahName = gSurahName;
        text = gText;
        tafseer = gTafseer;
    }

    public int getJuz() {
        return juz;
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

}
