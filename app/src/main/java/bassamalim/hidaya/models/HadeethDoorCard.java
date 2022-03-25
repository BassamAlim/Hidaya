package bassamalim.hidaya.models;

public class HadeethDoorCard {

    private final int doorId;
    private final String doorTitle;
    private final String text;
    private boolean fav;

    public HadeethDoorCard(int doorId, String doorTitle, String text, boolean fav) {
        this.doorId = doorId;
        this.doorTitle = doorTitle;
        this.text = text;
        this.fav = fav;
    }

    public int getDoorId() {
        return doorId;
    }

    public String getDoorTitle() {
        return doorTitle;
    }

    public String getText() {
        return text;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public boolean isFav() {
        return fav;
    }
}
