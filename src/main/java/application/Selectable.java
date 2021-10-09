package application;

import javafx.css.PseudoClass;

public interface Selectable {

    PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    void setSelected(boolean selected);
    boolean isSelected();

}
