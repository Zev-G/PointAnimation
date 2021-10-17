package application;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

public class EditableStringProperty extends EditableProperty<String> {

    private final TextArea editor = new TextArea();

    public EditableStringProperty(String name, Property<String> property) {
        super(name, property);
        editor.setText(property.getValue());
        property.bind(editor.textProperty());
    }

    @Override
    public Node getEditor() {
        return editor;
    }

}
