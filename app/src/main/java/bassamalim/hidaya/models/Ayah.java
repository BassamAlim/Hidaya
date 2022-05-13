package bassamalim.hidaya.models;

import android.text.SpannableString;
import android.widget.TextView;

public class Ayah {

    private int juz;
    private final int surahNum;
    private final int ayahNum;
    private final String surahName;
    private int pageNum;
    private String text;
    private String translation;
    private final String tafseer;
    private int start;
    private int end;
    private SpannableString ss;
    private int index;
    private TextView screen;

    public Ayah(int juz, int surahNum, int ayahNum, String surahName,
                String text, String translation, String tafseer) {
        this.juz = juz;
        this.surahNum = surahNum;
        this.ayahNum = ayahNum;
        this.surahName = surahName;
        this.text = text;
        this.translation = translation;
        this.tafseer = tafseer;
    }

    public Ayah(int surahNum, String surahName, int pageNum, int ayahNum,
                String tafseer, SpannableString ss) {
        this.surahNum = surahNum;
        this.ayahNum = ayahNum;
        this.pageNum = pageNum;
        this.surahName = surahName;
        this.tafseer = tafseer;
        this.ss = ss;
    }

    public int getJuz() {
        return juz;
    }

    public int getSurahNum() {
        return surahNum;
    }

    public int getAyahNum() {
        return ayahNum;
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

    public String getTranslation() {
        return translation;
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
