package application;

import application.shapes.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import json.ConnectionJSON;
import json.FrameJSON;
import json.JSONSavable;
import json.PointJSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FrameView extends Pane implements JSONSavable<FrameJSON> {

    private final ObjectProperty<Image> lastImage = new SimpleObjectProperty<>();

    private final Shape shape;
    private final Point imaginaryPoint = new Point();

    // Drag Variables:
    private Node intersected;
    private Connection dragConnection;
    private boolean dragging = false;
    private double lastX;
    private double lastY;

    private final ObservableList<Selectable> selected = FXCollections.observableArrayList();
    private final AppView app;

    public FrameView(AppView app) {
        this(app, new Shape());
    }
    public FrameView(AppView app, Shape shape) {
        this.shape = shape;
        this.app = app;
        getChildren().add(shape);

        selected.addListener((ListChangeListener<? super Selectable>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Selectable removed : change.getRemoved()) {
                        removed.setSelected(false);
                    }
                }
                if (change.wasAdded()) {
                    for (Selectable removed : change.getAddedSubList()) {
                        removed.setSelected(true);
                    }
                }
            }
        });

        Consumer<Node> visibilityHandler = added -> {
            if (!(added instanceof PointConnection)) {
                added.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
                    int val = app.showingAllProperty().get();
                    if (val == 0) {
                        return true;
                    } else if (val == 1 && !(added instanceof Point)) {
                        return false;
                    } else if (val == 2) {
                        return false;
                    }
                    return true;
                }, app.showingAllProperty()));
            }
        };
        shape.getChildrenUnmodifiable().forEach(visibilityHandler);
        shape.getChildrenUnmodifiable().addListener((ListChangeListener<? super Node>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(visibilityHandler);
                }
            }
        });

        setFocusTraversable(true);
        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                intersected = event.getPickResult().getIntersectedNode();
                if (intersected != null && intersected.getParent() instanceof Point) {
                    Point point = (Point) intersected.getParent();
                    new ArrayList<>(point.getConnectionsFrom()).forEach(Connection::remove);
                    new ArrayList<>(point.getConnectionsTo()).forEach(Connection::remove);
                    shape.getPoints().remove(point);
                } else if (intersected instanceof CurveToConnection) {
                    ((CurveToConnection) intersected).getConnection().remove();
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
                    dragConnection = Point.connect(intersectedPoint, imaginaryPoint, CurvedLineType.BASIC);
                } else if (intersected instanceof CurveToConnection) {
                    ((CurveToConnection) intersected).getConnection().remove();
                }
            } else if (event.getButton() == MouseButton.PRIMARY) {
                intersected = event.getPickResult().getIntersectedNode();
                if (intersected instanceof PointConnection) {
                    PointConnection pointConnection = (PointConnection) intersected;
                    dragConnection = pointConnection.getConnection();
                    dragging = true;
                    lastX = event.getX();
                    lastY = event.getY();
                } else if (intersected == this) {
                    intersected = null;
                    dragging = true;
                    lastX = event.getX();
                    lastY = event.getY();
                }
            }
        });
        setOnMouseDragged(event -> {
            if (dragging) {
                if (intersected == null) {
                    double x = event.getX();
                    double y = event.getY();
                    double deltaX = x - lastX;
                    double deltaY = y - lastY;
                    for (Point point : shape.getPoints()) {
                        point.setX(point.getX() + deltaX);
                        point.setY(point.getY() + deltaY);
                    }
                    lastX = x;
                    lastY = y;
                } else if (intersected instanceof PointConnection) {
                    Point start = dragConnection.getStart();
                    Point end = dragConnection.getEnd();
                    double x = event.getX();
                    double y = event.getY();
                    double deltaX = x - lastX;
                    double deltaY = y - lastY;
                    start.setX(start.getX() + deltaX);
                    start.setY(start.getY() + deltaY);
                    end.setX(end.getX() + deltaX);
                    end.setY(end.getY() + deltaY);
                    lastX = x;
                    lastY = y;
                } else {
                    imaginaryPoint.setX(event.getX());
                    imaginaryPoint.setY(event.getY());

                    Node intersectedTemp = event.getPickResult().getIntersectedNode();
                    if (intersectedTemp != null && intersectedTemp.getParent() instanceof Point && intersectedTemp != intersected) {
                        if (dragConnection.getEnd() != intersectedTemp.getParent()) {
                            dragConnection.remove();
                            dragConnection = Point.connect(dragConnection.getStart(), (Point) intersectedTemp.getParent(), CurvedLineType.BASIC);
                        }
                    } else if (dragConnection != null && dragConnection.getEnd() != imaginaryPoint) {
                        dragConnection.remove();
                        dragConnection = Point.connect(dragConnection.getStart(), imaginaryPoint, CurvedLineType.BASIC);
                    }
                }
            }
        });
        setOnMouseReleased(event -> {
            if (!dragging) {
                Node node = event.getPickResult().getIntersectedNode();
                if (node instanceof Selectable) {
                    selected.setAll((Selectable) node);
                }
            }
            dragging = false;
            if (dragConnection != null && dragConnection.getEnd() == imaginaryPoint) {
                dragConnection.remove();
            }
            dragConnection = null;
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
        FrameView frameView = new FrameView(app, shape.duplicate());
        frameView.lastImage.set(lastImage.get());
        return frameView;
    }

    public ObjectProperty<Image> lastImageProperty() {
        return lastImage;
    }

    public static FrameView fromJSON(FrameJSON json, AppView app) {
        FrameView view = new FrameView(app);
        view.apply(json);
        return view;
    }

    @Override
    public void apply(FrameJSON json) {
        shape.getPoints().clear();
        Point[] points = new Point[json.points.length];
        PointJSON[] pointJSONS = json.points;
        for (int i = 0, pointJSONSLength = pointJSONS.length; i < pointJSONSLength; i++) {
            PointJSON jsonPoint = pointJSONS[i];
            points[i] = new Point();
            points[i].setX(jsonPoint.x);
            points[i].setY(jsonPoint.y);
        }
        for (ConnectionJSON connection : json.connections) {
            Point.connect(points[connection.a], points[connection.b], CurvedLineType.BASIC);
        }
        shape.getPoints().addAll(points);
    }

    @Override
    public FrameJSON toJSON() {
        Map<Point, Integer> positionMap = new HashMap<>();
        PointJSON[] pointJSONS = new PointJSON[shape.getPoints().size()];
        for (int i = 0; i < shape.getPoints().size(); i++) {
            Point point = shape.getPoints().get(i);
            pointJSONS[i] = new PointJSON();
            pointJSONS[i].x = point.getX();
            pointJSONS[i].y = point.getY();
            positionMap.put(point, i);
        }
        ConnectionJSON[] connectionJSONS = new ConnectionJSON[shape.getConnectionsUnmodifiable().size()];
        ObservableList<Connection> connectionsUnmodifiable = shape.getConnectionsUnmodifiable();
        for (int i = 0, connectionsUnmodifiableSize = connectionsUnmodifiable.size(); i < connectionsUnmodifiableSize; i++) {
            Connection connection = connectionsUnmodifiable.get(i);
            ConnectionJSON connectionJSON = new ConnectionJSON();
            connectionJSON.a = positionMap.get(connection.getStart());
            connectionJSON.b = positionMap.get(connection.getEnd());
            connectionJSON.type = connection.getConnector().getName();
            connectionJSONS[i] = connectionJSON;
        }
        FrameJSON frameJSON = new FrameJSON();
        frameJSON.connections = connectionJSONS;
        frameJSON.points = pointJSONS;
        return frameJSON;
    }

}
