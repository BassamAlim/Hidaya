package com.bassamalim.athkar.models;

public class ReciterCard {

    private final int num;
    private final String name;
    private final RecitationVersion[] versions;

    public ReciterCard(int gNum, String gName, RecitationVersion[] gVersions) {
        num = gNum;
        name = gName;
        versions = gVersions;
    }

    public int getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public RecitationVersion[] getVersions() {
        return versions;
    }
}


