package application.shapes;

import application.PivotApplication;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.shape.Line;

public class LineConnection extends Line implements PointConnection {

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

}
