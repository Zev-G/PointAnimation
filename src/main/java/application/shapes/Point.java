package application.shapes;

import application.PivotApplication;
import com.me.tmw.properties.NodeProperty;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.stream.Collectors;

public class Point extends Parent {

    private final ObjectProperty<Shape> shape = new SimpleObjectProperty<>();
    private final NodeProperty visual = new NodeProperty() {
        @Override
        protected void invalidated() {
            Point.this.getChildren().setAll(visual.get());
        }
    };
    private final ObservableList<Connection> connectionsTo = FXCollections.observableArrayList();
    private final ObservableList<Connection> connectionsFrom = FXCollections.observableArrayList();

    private final BooleanProperty draggable = new SimpleBooleanProperty(true);
    private final double[] dragData = new double[4];
    private boolean dragging = false;

    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();

    public Point() {
        this(new Circle(PivotApplication.DEFAULT_SIZE, Color.ORANGE));
    }
    public Point(Node initial) {
        visual.set(initial);

        layoutXProperty().bind(x);
        layoutYProperty().bind(y);

        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !event.isShiftDown()) {
                dragData[0] = getX();
                dragData[1] = getY();
                dragData[2] = event.getScreenX();
                dragData[3] = event.getScreenY();
                dragging = true;
            }
        });
        setOnMouseDragged(event -> {
            if (dragging && draggable.get()) {
                setX(event.getScreenX() - dragData[2] + dragData[0]);
                setY(event.getScreenY() - dragData[3] + dragData[1]);
            }
        });
        setOnMouseReleased(event -> dragging = false);
    }

    public static Connection connect(Point pointA, Point pointB, LineType basic) {
        Connection connection = new Connection(pointA, pointB, basic);
        pointA.getConnectionsTo().add(connection);
        pointB.getConnectionsFrom().add(connection);
        return connection;
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public double getX() {
        return x.get();
    }

    public double getY() {
        return y.get();
    }

    public Node getVisual() {
        return visual.get();
    }

    public NodeProperty visualProperty() {
        return visual;
    }

    public void setVisual(Node visual) {
        this.visual.set(visual);
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public ObservableList<Connection> getConnectionsTo() {
        return connectionsTo;
    }

    public ObservableList<Connection> getConnectionsFrom() {
        return connectionsFrom;
    }

    public Shape getShape() {
        return shape.get();
    }

    public void setShape(Shape shape) {
        this.shape.set(shape);
    }

}
