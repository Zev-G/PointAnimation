package application;

import javafx.beans.property.Property;
import javafx.scene.Node;

public abstract class EditableProperty<T> {

    private final String name;
    private final Property<T> property;

    public EditableProperty(String name, Property<T> property) {
        this.name = name;
        this.property = property;
    }

    public abstract Node getEditor();

    public Property<T> getProperty() {
        return property;
    }
    public T get() {
        return property.getValue();
    }
    public void set(T val) {
        property.setValue(val);
    }

    public String getName() {
        return name;
    }

}
