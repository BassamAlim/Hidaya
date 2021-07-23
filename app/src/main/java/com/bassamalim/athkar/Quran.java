package com.bassamalim.athkar;

public class Quran {

    int code;
    String status;
    Data data;

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }
}

class Data {

    Surah[] surahs;
    Edition edition;

    public Surah[] getSurahs() {
        return surahs;
    }

    public Edition getEdition() {
        return edition;
    }
}

class Surah {

    int number;
    String name;
    String englishName;
    String englishNameTranslation;
    String revelationType;
    Ayah[] ayahs;

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getEnglishNameTranslation() {
        return englishNameTranslation;
    }

    public String getRevelationType() {
        return revelationType;
    }

    public Ayah[] getAyahs() {
        return ayahs;
    }
}

class Edition {

    String identifier;
    String language;
    String name;
    String englishName;
    String format;
    String type;

    public String getIdentifier() {
        return identifier;
    }

    public String getLanguage() {
        return language;
    }

    public String getName() {
        return name;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }
}

class Ayah {

    int number;
    String text;
    int numberInSurah;
    int juz;
    int manzil;
    int page;
    int ruku;
    int hizbQuarter;
    boolean sajda;

    public int getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }

    public int getNumberInSurah() {
        return numberInSurah;
    }

    public int getJuz() {
        return juz;
    }

    public int getManzil() {
        return manzil;
    }

    public int getPage() {
        return page;
    }

    public int getRuku() {
        return ruku;
    }

    public int getHizbQuarter() {
        return hizbQuarter;
    }

    public boolean isSajda() {
        return sajda;
    }
}