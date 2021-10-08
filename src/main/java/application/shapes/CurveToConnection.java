package application.shapes;

import application.PivotApplication;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.QuadCurve;

import java.util.function.Supplier;

public class CurveToConnection extends QuadCurve implements PointConnection {

    private final DoubleProperty controlXRelative = new SimpleDoubleProperty();
    private final DoubleProperty controlYRelative = new SimpleDoubleProperty();

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
    public CurveToConnection() {
        this(null);
    }
    public CurveToConnection(Connection connection) {
        setConnection(connection);

        Supplier<Double> numGen = () -> Math.random() * 250;
        controlXProperty().bind(controlXRelative.add(startXProperty().add(endXProperty()).divide(2)));
        controlYProperty().bind(controlYRelative.add(startYProperty().add(endYProperty()).divide(2)));
        setStrokeWidth(PivotApplication.DEFAULT_SIZE * 0.75);
        setStroke(Color.BLACK);
        setFill(Color.TRANSPARENT);

    }

    public DoubleProperty controlXRelativeProperty() {
        return controlXRelative;
    }

    public DoubleProperty controlYRelativeProperty() {
        return controlYRelative;
    }

    public void setConnection(Connection connection) {
        this.connection.set(connection);
    }

    @Override
    public Connection getConnection() {
        return connection.get();
    }

    @Override
    public LineType getLineType() {
        return SimpleLineType.BASIC;
    }

    @Override
    public Node node() {
        return this;
    }
}
