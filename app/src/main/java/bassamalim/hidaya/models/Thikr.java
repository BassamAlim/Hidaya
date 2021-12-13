package bassamalim.hidaya.models;

public class Thikr {

    private final String title;
    private final String text;
    private final String repetition;
    private final String fadl;
    private final String reference;

    public Thikr(String gTitle, String gText, String gRepetition, String gFadl, String gReference) {
        title = gTitle;
        text = gText;
        repetition = gRepetition;
        fadl = gFadl;
        reference = gReference;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getRepetition() {
        return repetition;
    }

    public String getFadl() {
        return fadl;
    }

    public String getReference() {
        return reference;
    }
}
