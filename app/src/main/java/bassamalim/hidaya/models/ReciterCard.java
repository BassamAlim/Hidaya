package bassamalim.hidaya.models;

import android.view.View;

import java.io.Serializable;

public class ReciterCard {

    private final String name;
    private final RecitationVersion[] versions;

    public ReciterCard(String gName, RecitationVersion[] gVersions) {
        name = gName;
        versions = gVersions;
    }

    public String getName() {
        return name;
    }

    public RecitationVersion[] getVersions() {
        return versions;
    }

    public static class RecitationVersion implements Serializable {
        private final String server;
        private final String rewaya;
        private final int count;
        private final String suras;
        private final View.OnClickListener listener;

        public RecitationVersion(String server, String rewaya, int count,
                                 String suras, View.OnClickListener listener) {
            this.server = server;
            this.rewaya = rewaya;
            this.count = count;
            this.suras = suras;
            this.listener = listener;
        }

        public String getServer() {
            return server;
        }

        public String getRewaya() {
            return rewaya;
        }

        public int getCount() {
            return count;
        }

        public String getSuras() {
            return suras;
        }

        public View.OnClickListener getListener() {
            return listener;
        }
    }
}


