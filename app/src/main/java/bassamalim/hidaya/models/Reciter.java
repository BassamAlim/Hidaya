package bassamalim.hidaya.models;

import android.view.View;

import java.io.Serializable;
import java.util.List;

public class Reciter {

    private final int id;
    private final String name;
    private int favorite;
    private final List<RecitationVersion> versions;

    public Reciter(int id, String name, int favorite, List<RecitationVersion> versions) {
        this.id = id;
        this.name = name;
        this.favorite = favorite;
        this.versions = versions;
    }

    public int getId() {
        return id;
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

    public List<RecitationVersion> getVersions() {
        return versions;
    }

    public static class RecitationVersion implements Serializable {
        private final int versionId;
        private final String server;
        private final String rewaya;
        private final int count;
        private final String suras;
        private final View.OnClickListener listener;

        public RecitationVersion(int versionId, String server, String rewaya, int count,
                                 String suras, View.OnClickListener listener) {
            this.versionId = versionId;
            this.server = server;
            this.rewaya = rewaya;
            this.count = count;
            this.suras = suras;
            this.listener = listener;
        }

        public int getVersionId() {
            return versionId;
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

