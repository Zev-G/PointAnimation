package application;

import application.shapes.Point;
import javafx.css.PseudoClass;

public interface Selectable {

    PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    void setSelected(boolean selected);
    boolean isSelected();

    Point[] getPoints();
    void remove(FrameView view);

    default EditableProperty<?>[] getEditableProperties() {
        return new EditableProperty[0];
    }

}
