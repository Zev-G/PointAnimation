package application.shapes;

import application.PivotApplication;
import application.Selectable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.shape.Line;

public class LineConnection extends Line implements PointConnection {

    private final BooleanProperty selected = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(Selectable.SELECTED, selected.get());
        }
    };

    private final ObjectProperty<Connection> connection = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Point start = get().getStart();
            if (start == null) {
                startXProperty().unbind();
                startYProperty().unbind();
            } else {
                startXProperty().bind(start.xProperty());
                startYProperty().bind(start.yProperty());
            }
            Point end = get().getEnd();
            if (end == null) {
                endXProperty().unbind();
                endYProperty().unbind();
            } else {
                endXProperty().bind(end.xProperty());
                endYProperty().bind(end.yProperty());
            }
        }
    };

    public LineConnection() {
        this(null);
    }
    public LineConnection(Connection connection) {
        setConnection(connection);
        setStrokeWidth(PivotApplication.DEFAULT_SIZE * 0.75);
        setFocusTraversable(true);
        setOnMousePressed(event -> requestFocus());
    }

    @Override
    public LineType getLineType() {
        return SimpleLineType.BASIC;
    }

    @Override
    public Node node() {
        return this;
    }

    @Override
    public Connection getConnection() {
        return connection.get();
    }

    public ObjectProperty<Connection> connectionProperty() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection.set(connection);
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public boolean isSelected() {
        return selected.get();
    }

}
