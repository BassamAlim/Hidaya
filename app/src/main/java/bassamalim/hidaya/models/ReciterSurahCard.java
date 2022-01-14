package bassamalim.hidaya.models;

public class ReciterSurahCard {

    private final int num;
    private final String surahName;
    private final String searchName;

    public ReciterSurahCard(int gNum, String gName, String gSearchName) {
        num = gNum;
        surahName = gName;
        searchName = gSearchName;
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
}
