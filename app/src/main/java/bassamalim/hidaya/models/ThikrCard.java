package bassamalim.hidaya.models;

public class ThikrCard {

    private final int id;
    private final String title;
    private final String text;
    private final String fadl;
    private final String reference;
    private final String repetition;

    public ThikrCard(int id, String title, String text, String fadl,
                     String reference, String repetition) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.fadl = fadl;
        this.reference = reference;
        this.repetition = repetition;
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

    public String getFadl() {
        return fadl;
    }

    public String getReference() {
        return reference;
    }

    public String getRepetition() {
        return repetition;
    }
}
