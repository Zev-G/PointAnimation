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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import json.ConnectionJSON;
import json.FrameJSON;
import json.JSONSavable;
import json.PointJSON;

import java.util.*;
import java.util.function.Consumer;

public class FrameView extends Pane implements JSONSavable<FrameJSON> {

    private static final String STYLE_SHEET = Res.css("frame-view");

    private final ObjectProperty<Image> lastImage = new SimpleObjectProperty<>();

    private final Shape shape;
    private final Point imaginaryPoint = new Point();

    // Drag Variables:
    private Node intersected;
    private Connection dragConnection;
    private boolean dragging = false;
    private double lastX;
    private double lastY;

    private double startX;
    private double startY;

    private final ObservableList<Selectable> selected = FXCollections.observableArrayList();
    private final AppView app;

    private final Rectangle selectionRectangle = new Rectangle();

    public FrameView(AppView app) {
        this(app, new Shape());
    }
    public FrameView(AppView app, Shape shape) {
        this.shape = shape;
        this.app = app;
        getStylesheets().add(STYLE_SHEET);
        getChildren().addAll(shape, selectionRectangle);

        selectionRectangle.setMouseTransparent(true);
        selectionRectangle.setFill(Color.rgb(23, 183, 246, 0.3));
        selectionRectangle.setStroke(Color.rgb(19, 62, 100, 0.8));
        selectionRectangle.getStrokeDashArray().setAll(10D);

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
                if (c.wasRemoved()) {
                    for (Node node : c.getRemoved()) {
                        if (node instanceof Selectable) selected.remove(node);
                    }
                }
            }
        });

        setFocusTraversable(true);
        setOnMousePressed(event -> {
            startX = event.getX();
            startY = event.getY();
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
                    if (event.getButton() != MouseButton.PRIMARY) {
                        double deltaX = x - lastX;
                        double deltaY = y - lastY;
                        for (Point point : shape.getPoints()) {
                            point.setX(point.getX() + deltaX);
                            point.setY(point.getY() + deltaY);
                        }
                        lastX = x;
                        lastY = y;
                    } else if (!event.isShiftDown()) {
                        if (selected.isEmpty() || selectionRectangle.getWidth() != 0 || selectionRectangle.getHeight() != 0) {
                            selectionRectangle.setX(startX);
                            selectionRectangle.setY(startY);
                            double width = x - startX;
                            double height = y - startY;
                            selectionRectangle.setWidth(Math.abs(width));
                            selectionRectangle.setHeight(Math.abs(height));
                            if (width < 0) {
                                selectionRectangle.setX(startX + width);
                            }
                            if (height < 0) {
                                selectionRectangle.setY(startY + height);
                            }
                            selectInRectangle(selectionRectangle.getX(), selectionRectangle.getY(), selectionRectangle.getX() + selectionRectangle.getWidth(), selectionRectangle.getY() + selectionRectangle.getHeight());
                        } else {
                            double deltaX = x - lastX;
                            double deltaY = y - lastY;
                            Set<Point> toBeMoved = new HashSet<>();
                            for (Selectable selectable : selected) {
                                toBeMoved.addAll(Arrays.asList(selectable.getPoints()));
                            }
                            for (Point point : toBeMoved) {
                                point.setX(point.getX() + deltaX);
                                point.setY(point.getY() + deltaY);
                            }
                            lastX = x;
                            lastY = y;
                        }
                    }
                } else if (intersected instanceof PointConnection) {
                    Point start = dragConnection.getStart();
                    Point end = dragConnection.getEnd();
                    double x = event.getX();
                    double y = event.getY();
                    double deltaX = x - lastX;
                    double deltaY = y - lastY;
                    if (!selected.contains(intersected)) {
                        start.setX(start.getX() + deltaX);
                        start.setY(start.getY() + deltaY);
                        end.setX(end.getX() + deltaX);
                        end.setY(end.getY() + deltaY);
                    } else {
                        Set<Point> toBeMoved = new HashSet<>();
                        for (Selectable selectable : selected) {
                            toBeMoved.addAll(Arrays.asList(selectable.getPoints()));
                        }
                        for (Point point : toBeMoved) {
                            point.setX(point.getX() + deltaX);
                            point.setY(point.getY() + deltaY);
                        }
                    }
                    lastX = x;
                    lastY = y;
                } else {
                    imaginaryPoint.setX(event.getX());
                    imaginaryPoint.setY(event.getY());

                    Node intersectedTemp = event.getPickResult().getIntersectedNode();
                    if (intersectedTemp != null && intersectedTemp.getParent() instanceof Point && intersectedTemp != intersected && dragConnection != null) {
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
            selectionRectangle.setWidth(0);
            selectionRectangle.setHeight(0);
            if (startX == event.getX() && startY == event.getY() && event.getClickCount() == 1) {
                Node node = event.getPickResult().getIntersectedNode();
                Selectable found = null;
                while (node.getParent() != null && found == null && node != getParent()) {
                    if (node instanceof Selectable) found = (Selectable) node;
                    node = node.getParent();
                }
                if (found != null) {
                    if (event.isShiftDown()) {
                        if (selected.contains(found)) {
                            selected.remove(found);
                        } else {
                            selected.add(found);
                        }
                    } else {
                        selected.setAll(found);
                    }
                } else if (!event.isShiftDown()) {
                    selected.clear();
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

    private void selectInRectangle(double x1, double y1, double x2, double y2) {
        for (Node node : shape.getChildrenUnmodifiable()) {
            Selectable found = null;
            Node search = node;
            while (search.getParent() != null && search != this) {
                if (search instanceof Selectable) {
                    found = (Selectable) search;
                    break;
                }
                search = search.getParent();
            }
            if (found != null) {
                double x;
                double y;
                if (found instanceof CurveToConnection) {
                    CurveToConnection curvedConnection = (CurveToConnection) found;
                    x = (curvedConnection.getStartX() + curvedConnection.getEndX()) / 2;
                    y = (curvedConnection.getStartY() + curvedConnection.getEndY()) / 2;
                } else {
                    x = node.getLayoutX();
                    y = node.getLayoutY();
                }
                if (x > x1 && x < x2 && y > y1 && y < y2) {
                    if (!selected.contains(found)) selected.add(found);
                } else {
                    selected.remove(found);
                }
            }
        }
    }

    public void selectAll() {
        for (Node child : shape.getChildrenUnmodifiable()) {
            if (child instanceof Selectable && !selected.contains(child)) {
                selected.add((Selectable) child);
            }
        }
    }
    public void clearSelection() {
        selected.clear();
    }
    public void deleteSelectedItems() {
        for (Selectable selected : new ArrayList<>(this.selected)) {
            if (selected instanceof CurveToConnection) {
                ((CurveToConnection) selected).getConnection().remove();
            } else if (selected instanceof Point) {
                shape.getPoints().remove(selected);
            }
        }
        takeSnapshot();
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

    public Shape getFrameShape() {
        return shape;
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

    public ObservableList<Selectable> getSelected() {
        return selected;
    }

}
