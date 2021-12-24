package bassamalim.hidaya.models;

public class JTafseer {
    private int code;
    private String status;
    private Data data;

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        private Surah[] surahs;
        private Edition edition;

        public Surah[] getSurahs() {
            return surahs;
        }

        public Edition getEdition() {
            return edition;
        }

        public class Surah {
            private int number;
            private String name;
            private String englishName;
            private String englishNameTranslation;
            private String revelationType;
            private JTAyah[] ayahs;

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

            public JTAyah[] getAyahs() {
                return ayahs;
            }

            public class JTAyah {
                private int number;
                private String text;
                private int numberInSurah;
                private int juz;
                private int manzil;
                private int page;
                private int ruku;
                private int hizbQuarter;
                private Sajda sajda;

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

                public Sajda isSajda() {
                    return sajda;
                }

                public class Sajda {
                    private boolean exists;
                    private int id;
                    private boolean recommended;
                    private boolean obligatory;
                }
            }
        }
    }

    class Edition {
        private String identifier;
        private String language;
        private String name;
        private String englishName;
        private String format;
        private String type;

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
}
