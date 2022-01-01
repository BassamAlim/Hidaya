package bassamalim.hidaya.models;

import android.view.View;

import java.io.Serializable;

public class ReciterCard {

    private final String name;
    private int favorite;
    private final RecitationVersion[] versions;

    public ReciterCard(String name, int favorite, RecitationVersion[] versions) {
        this.name = name;
        this.favorite = favorite;
        this.versions = versions;
    }

    public String getName() {
        return name;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getFavorite() {
        return favorite;
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


