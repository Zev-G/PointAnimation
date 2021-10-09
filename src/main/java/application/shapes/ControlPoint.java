package application.shapes;

import application.PivotApplication;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class ControlPoint extends Parent {

    private final ObjectProperty<Node> visual = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            getChildren().setAll(visual.get());
        }
    };

    private final BooleanProperty draggable = new SimpleBooleanProperty(true);
    private final double[] dragData = new double[4];
    private boolean dragging = false;

    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();

    protected final Connection connection;
    protected final List<Node> attachedLines = new ArrayList<>();

    public ControlPoint(Connection connection) {
        this.connection = connection;
        setVisual(new Circle(PivotApplication.DEFAULT_SIZE * 0.75, Color.BLUE));

        Line a = new Line();
        a.endXProperty().bind(connection.getStart().layoutXProperty());
        a.endYProperty().bind(connection.getStart().layoutYProperty());
        a.startXProperty().bind(layoutXProperty());
        a.startYProperty().bind(layoutYProperty());

        Line b = new Line();
        b.endXProperty().bind(connection.getEnd().layoutXProperty());
        b.endYProperty().bind(connection.getEnd().layoutYProperty());
        b.startXProperty().bind(layoutXProperty());
        b.startYProperty().bind(layoutYProperty());

        a.getStrokeDashArray().setAll(20D);
        a.setStroke(Color.GRAY);
        a.setStrokeDashOffset(10);
        a.setStrokeWidth(PivotApplication.DEFAULT_SIZE * 0.5);

        b.getStrokeDashArray().setAll(20D);
        b.setStroke(Color.GRAY);
        b.setStrokeDashOffset(10);
        b.setStrokeWidth(PivotApplication.DEFAULT_SIZE * 0.5);

        attachedLines.add(a);
        attachedLines.add(b);

        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !event.isShiftDown()) {
                dragData[0] = getX();
                dragData[1] = getY();
                dragData[2] = event.getScreenX();
                dragData[3] = event.getScreenY();
                dragging = true;
            }
        });
        DoubleBinding midX = connection.getStart().xProperty().add(connection.getEnd().xProperty()).divide(2);
        DoubleBinding midY = connection.getStart().yProperty().add(connection.getEnd().yProperty()).divide(2);
        setOnMouseDragged(event -> {
            if (dragging && draggable.get()) {
                setX((event.getScreenX() - dragData[2] + dragData[0]));
                setY((event.getScreenY() - dragData[3] + dragData[1]));
            }
        });
        layoutXProperty().bind(xProperty().add(midX));
        layoutYProperty().bind(yProperty().add(midY));
    }

    public Connection getConnection() {
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

    public void setX(double x) {
        this.x.set(x);
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public Node getVisual() {
        return visual.get();
    }

    public ObjectProperty<Node> visualProperty() {
        return visual;
    }

    public void setVisual(Node visual) {
        this.visual.set(visual);
    }

    public List<Node> getAttachedLines() {
        return attachedLines;
    }

}
