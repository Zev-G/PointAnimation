package application;

import application.shapes.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class FrameView extends Pane {

    private final ObjectProperty<Image> lastImage = new SimpleObjectProperty<>();

    private final Shape shape;
    private final Point imaginaryPoint = new Point();

    // Drag Variables:
    private Node intersected;
    private Connection dragConnection;
    private boolean dragging = false;

    public FrameView() {
        this(new Shape());
    }
    public FrameView(Shape shape) {
        this.shape = shape;
        getChildren().add(shape);

        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                intersected = event.getPickResult().getIntersectedNode();
                if (intersected != null && intersected.getParent() instanceof Point) {
                    shape.getPoints().remove((Point) intersected.getParent());
                } else if (intersected instanceof LineConnection) {
                    ((LineConnection) intersected).getConnection().remove();
                }
                takeSnapshot();
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !event.isShiftDown()) {
                Point newPoint = new Point();
                newPoint.setX(event.getX());
                newPoint.setY(event.getY());
                shape.getPoints().add(newPoint);
            } else if (event.getButton() == MouseButton.SECONDARY || (event.getButton() == MouseButton.PRIMARY && event.isShiftDown())) {
                dragging = true;
                imaginaryPoint.setX(event.getX());
                imaginaryPoint.setY(event.getY());
                intersected = event.getPickResult().getIntersectedNode();
                if (intersected != null && intersected.getParent() instanceof Point) {
                    Point intersectedPoint = (Point) intersected.getParent();
                    dragConnection = Point.connect(intersectedPoint, imaginaryPoint, SimpleLineType.BASIC);
                } else if (intersected instanceof LineConnection) {
                    ((LineConnection) intersected).getConnection().remove();
                }
            }
        });
        setOnMouseDragged(event -> {
            if (dragging) {
                imaginaryPoint.setX(event.getX());
                imaginaryPoint.setY(event.getY());

                Node intersectedTemp = event.getPickResult().getIntersectedNode();
                if (intersectedTemp != null && intersectedTemp.getParent() instanceof Point && intersectedTemp != intersected) {
                    if (dragConnection.getEnd() != intersectedTemp.getParent()) {
                        dragConnection.remove();
                        dragConnection = Point.connect(dragConnection.getStart(), (Point) intersectedTemp.getParent(), SimpleLineType.BASIC);
                    }
                } else if (dragConnection != null && dragConnection.getEnd() != imaginaryPoint) {
                    dragConnection.remove();
                    dragConnection = Point.connect(dragConnection.getStart(), imaginaryPoint, SimpleLineType.BASIC);
                }
            }
        });
        setOnMouseReleased(event -> {
            dragging = false;
            if (dragConnection != null && dragConnection.getEnd() == imaginaryPoint) {
                dragConnection.remove();
            }
            takeSnapshot();
        });
    }

    public void takeSnapshot() {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(0, 0, 1000, 500));
        lastImage.set(snapshot(parameters, null));
    }

    public FrameView duplicate() {
        FrameView frameView = new FrameView(shape.duplicate());
        frameView.lastImage.set(lastImage.get());
        return frameView;
    }

    public ObjectProperty<Image> lastImageProperty() {
        return lastImage;
    }

}
