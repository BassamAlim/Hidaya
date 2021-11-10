package com.bassamalim.athkar.models;

import android.view.View;

public class RecitationVersion {
    private final String server;
    private final String rewaya;
    private final String count;
    private final String suras;
    private final View.OnClickListener listener;

    public RecitationVersion(String gServer, String gRewaya, String gCount, String gSuras,
                             View.OnClickListener gListener) {
        server = gServer;
        rewaya = gRewaya;
        count = gCount;
        suras = gSuras;
        listener = gListener;
    }

    public String getServer() {
        return server;
    }

    public String getRewaya() {
        return rewaya;
    }

    public String getCount() {
        return count;
    }

    public String getSuras() {
        return suras;
    }

    public View.OnClickListener getListener() {
        return listener;
    }
}
