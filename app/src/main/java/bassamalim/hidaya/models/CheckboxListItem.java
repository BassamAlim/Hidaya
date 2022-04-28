package bassamalim.hidaya.models;

public class CheckboxListItem {

    private final String text;
    private boolean selected;

    public CheckboxListItem(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }

    public String getText() {
        return text;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
