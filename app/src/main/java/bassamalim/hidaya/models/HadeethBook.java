package bassamalim.hidaya.models;

public class HadeethBook {

    private final BookInfo bookInfo;
    private final BookChapter[] chapters;

    public HadeethBook(BookInfo bookInfo, BookChapter[] chapters) {
        this.bookInfo = bookInfo;
        this.chapters = chapters;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public BookChapter[] getChapters() {
        return chapters;
    }

    public static class BookInfo {

        private final int bookId;
        private final String bookTitle;
        private final String author;

        public BookInfo(int bookId, String bookTitle, String author) {
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.author = author;
        }

        public int getBookId() {
            return bookId;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public String getAuthor() {
            return author;
        }
    }

    public static class BookChapter {

        private final int chapterId;
        private final String chapterTitle;
        private final BookDoor[] doors;

        public BookChapter(int chapterId, String chapterTitle, BookDoor[] doors) {
            this.chapterId = chapterId;
            this.chapterTitle = chapterTitle;
            this.doors = doors;
        }

        public int getChapterId() {
            return chapterId;
        }

        public String getChapterTitle() {
            return chapterTitle;
        }

        public BookDoor[] getDoors() {
            return doors;
        }


        public static class BookDoor {

            private final int doorId;
            private final String doorTitle;
            private final String text;

            public BookDoor(int doorId, String doorTitle, String text) {
                this.doorId = doorId;
                this.doorTitle = doorTitle;
                this.text = text;
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
        }

    }

}
