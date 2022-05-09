package bassamalim.hidaya.models;

import android.view.View;

public class Thikr {

    private final int id;
    private final String title;
    private final String text;
    private final String textTranslation;
    private final String fadl;
    private final String reference;
    private final String repetition;
    private final View.OnClickListener referenceListener;

    public Thikr(int id, String title, String text, String textTranslation, String fadl,
                 String reference, String repetition, View.OnClickListener referenceListener) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.textTranslation = textTranslation;
        this.fadl = fadl;
        this.reference = reference;
        this.repetition = repetition;
        this.referenceListener = referenceListener;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getTextTranslation() {
        return textTranslation;
    }

    public String getFadl() {
        return fadl;
    }

    public String getReference() {
        return reference;
    }

    public String getRepetition() {
        return repetition;
    }

    public View.OnClickListener getReferenceListener() {
        return referenceListener;
    }
}
