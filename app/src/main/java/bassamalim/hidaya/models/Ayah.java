package bassamalim.hidaya.models;

import android.text.SpannableString;
import android.widget.TextView;

public class Ayah {

    private int juz;
    private final int surah;
    private final int ayah;
    private final String surahName;
    private int pageNum;
    private String text;
    private final String tafseer;
    private int start;
    private int end;
    private SpannableString ss;
    private int index;
    private TextView screen;

    public Ayah(int juz, int surah, int ayah, String surahName, String text, String tafseer) {
        this.juz = juz;
        this.surah = surah;
        this.ayah = ayah;
        this.surahName = surahName;
        this.text = text;
        this.tafseer = tafseer;
    }

    public Ayah(int surah, String surahName, int pageNum, int ayah,
                String tafseer, SpannableString ss) {
        this.surah = surah;
        this.ayah = ayah;
        this.pageNum = pageNum;
        this.surahName = surahName;
        this.tafseer = tafseer;
        this.ss = ss;
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

    public int getPageNum() {
        return pageNum;
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
