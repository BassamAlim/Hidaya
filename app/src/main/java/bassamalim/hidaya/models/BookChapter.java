package bassamalim.hidaya.models;

import android.view.View;

public class BookChapter {

    private final int chapterId;
    private final String chapterTitle;
    private boolean favorite;
    private final View.OnClickListener listener;

    public BookChapter(int chapterId, String chapterTitle, boolean favorite,
                       View.OnClickListener listener) {

        this.chapterId = chapterId;
        this.chapterTitle = chapterTitle;
        this.favorite = favorite;
        this.listener = listener;
    }

    public int getChapterId() {
        return chapterId;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public View.OnClickListener getListener() {
        return listener;
    }
}