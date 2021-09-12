package com.bassamalim.athkar.models;

public class Ayah {

    private String text;
    private final String tafseer;
    private int start;
    private int end;

    public Ayah(String gText, String gTafseer) {
        text = gText;
        tafseer = gTafseer;
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
