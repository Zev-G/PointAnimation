package application.shapes;

import application.FrameView;
import application.PivotApplication;
import application.Selectable;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Shadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.StrokeLineCap;

import java.util.Collections;
import java.util.List;

public class CurveToConnection extends QuadCurve implements PointConnection, Selectable {

    private final DoubleProperty relativeControlX = new SimpleDoubleProperty();
    private final DoubleProperty relativeControlY = new SimpleDoubleProperty();

    private final ControlPoint controlPoint;

    private final BooleanProperty selected = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            setEffect(get() ? selectedEffect : null);
        }
    };

    private final Connection connection;
    private final Effect selectedEffect;

    public CurveToConnection(Connection connection) {
        this.connection = connection;
        this.controlPoint = new ControlPoint(connection);

        DropShadow effect = new DropShadow();
        effect.setRadius(2);
        effect.setSpread(1);
        effect.setColor(Color.rgb(255,0,0,1));
        selectedEffect = effect;

        setPickOnBounds(false);
        setFill(Color.TRANSPARENT);
        setStroke(Color.BLACK);
        setStrokeWidth(PivotApplication.DEFAULT_SIZE * 0.75);
        setStrokeLineCap(StrokeLineCap.ROUND);

        controlXProperty().bind(controlPoint.layoutXProperty());
        controlYProperty().bind(controlPoint.layoutYProperty());

        Point start = connection.getStart();
        if (start == null) {
            startXProperty().unbind();
            startYProperty().unbind();
        } else {
            startXProperty().bind(start.xProperty());
            startYProperty().bind(start.yProperty());
        }
        Point end = connection.getEnd();
        if (end == null) {
            endXProperty().unbind();
            endYProperty().unbind();
        } else {
            endXProperty().bind(end.xProperty());
            endYProperty().bind(end.yProperty());
        }
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public boolean isSelected() {
        return selected.get();
    }

    @Override
    public Point[] getPoints() {
        return new Point[]{ connection.getStart(), connection.getEnd() };
    }

    @Override
    public void remove(FrameView view) {
        getConnection().remove();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public LineType getLineType() {
        return CurvedLineType.BASIC;
    }

    @Override
    public Node node() {
        return this;
    }

    @Override
    public List<ControlPoint> getControlPoints() {
        return Collections.singletonList(controlPoint);
    }

    public double getRelativeControlX() {
        return relativeControlX.get();
    }

    public DoubleProperty relativeControlXProperty() {
        return relativeControlX;
    }

    public void setRelativeControlX(double relativeControlX) {
        this.relativeControlX.set(relativeControlX);
    }

    public double getRelativeControlY() {
        return relativeControlY.get();
    }

    public DoubleProperty relativeControlYProperty() {
        return relativeControlY;
    }

    public void setRelativeControlY(double relativeControlY) {
        this.relativeControlY.set(relativeControlY);
    }

}
