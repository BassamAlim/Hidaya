package bassamalim.hidaya.models;

import android.view.View;

public class AlathkarButton {

    private final int id;
    private final int category_id;
    private final String name;
    private int favorite;
    private final View.OnClickListener listener;

    public AlathkarButton(int id, int category_id, String name, int favorite,
                          View.OnClickListener listener) {
        this.id = id;
        this.category_id = category_id;
        this.name = name;
        this.favorite = favorite;
        this.listener = listener;
    }

    public int getId() {
        return id;
    }

    public int getCategory_id() {
        return category_id;
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

    public View.OnClickListener getListener() {
        return listener;
    }
}
