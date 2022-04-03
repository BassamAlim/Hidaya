package bassamalim.hidaya.models;

import android.text.Spannable;

public class HadeethSearcherMatch {

    private final int bookId;
    private final String bookTitle;
    private final int chapterId;
    private final String chapterTitle;
    private final int doorId;
    private final String doorTitle;
    private final Spannable text;

    public HadeethSearcherMatch(int bookId, String bookTitle, int chapterId, String chapterTitle,
                                int doorId, String doorTitle, Spannable text) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.chapterId = chapterId;
        this.chapterTitle = chapterTitle;
        this.doorId = doorId;
        this.doorTitle = doorTitle;
        this.text = text;
    }

    public int getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public int getChapterId() {
        return chapterId;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public int getDoorId() {
        return doorId;
    }

    public String getDoorTitle() {
        return doorTitle;
    }

    public Spannable getText() {
        return text;
    }
}
