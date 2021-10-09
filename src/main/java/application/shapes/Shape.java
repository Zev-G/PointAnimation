package application.shapes;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import util.FXLists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shape extends Parent {

    private final ObservableList<Point> points = FXCollections.observableArrayList();
    private final ObservableList<ObservableList<Connection>> connectionLists = FXLists.map(points, Point::getConnectionsTo);
    private final ObservableList<Connection> connections = FXLists.reduce(connectionLists);

    private final Map<Connection, List<Node>> connectionNodeMap = new HashMap<>();
    private final Map<Connection, List<ControlPoint>> controlPointMap = new HashMap<>();
    private final ObservableList<ControlPoint> controlPoints = FXCollections.observableArrayList();

    public Shape() {

        points.addListener((ListChangeListener<Point>) c -> {
            while (c.next()) {
                c.getAddedSubList().forEach(point -> {
                    if (point.getShape() != null) {
                        point.setShape(this);
                        point.getShape().getPoints().remove(point);
                    } else {
                        point.setShape(this);
                    }
                });
                getChildren().addAll(c.getAddedSubList());
                c.getRemoved().forEach(point -> {
                    if (point.getShape() == this) {
                        point.setShape(null);
                    }
                });
                getChildren().removeAll(c.getRemoved());
            }
        });
        connections.addListener((ListChangeListener<Connection>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Connection added : c.getAddedSubList()) {
                        if (!connectionNodeMap.containsKey(added)) {
                            List<Node> nodes = new ArrayList<>();
                            PointConnection connection = added.createNode();
                            nodes.add(connection.node());
                            List<ControlPoint> controlPoints = connection.getControlPoints();
                            if (!controlPoints.isEmpty()) {
                                nodes.addAll(controlPoints);
                                for (ControlPoint point : controlPoints) {
                                    List<Node> attachedLines = point.getAttachedLines();
                                    if (!attachedLines.isEmpty()) {
                                        nodes.addAll(0, attachedLines);
                                    }
                                }
                                controlPointMap.put(added, controlPoints);
                            }
                            connectionNodeMap.put(added, nodes);
                        }
                        getChildren().addAll(0, connectionNodeMap.get(added));
                    }
                }
                if (c.wasRemoved()) {
                    for (Connection removed : c.getRemoved()) {
                        if (connectionNodeMap.containsKey(removed)) {
                            getChildren().removeAll(connectionNodeMap.get(removed));
                        }
                        if (controlPointMap.containsKey(removed)) {
                            controlPoints.removeAll(controlPointMap.get(removed));
                        }
                    }
                }
            }
        });
    }

    public Shape duplicate() {
        Shape shape = new Shape();
        Map<Point, Point> oldToNewPoints = new HashMap<>();
        for (Point oldPoint : getPoints()) {
            Point point = new Point();
            point.setX(oldPoint.getX());
            point.setY(oldPoint.getY());
            shape.getPoints().add(point);
            oldToNewPoints.put(oldPoint, point);
        }
        for (Connection connection : connections) {
            Connection newConnection = Point.connect(oldToNewPoints.get(connection.getStart()), oldToNewPoints.get(connection.getEnd()), connection.getConnector());
            List<ControlPoint> newControlPoints = shape.controlPointMap.get(newConnection);
            List<ControlPoint> oldControlPoints = controlPointMap.get(connection);
            if (newControlPoints != null && oldControlPoints != null) {
                for (int i = 0; i < newControlPoints.size(); i++) {
                    if (i < oldControlPoints.size()) {
                        ControlPoint newControlPoint = newControlPoints.get(i);
                        ControlPoint oldControlPoint = oldControlPoints.get(i);
                        newControlPoint.setX(oldControlPoint.getX());
                        newControlPoint.setY(oldControlPoint.getY());
                    }
                }
            }
        }
        return shape;
    }

    public ObservableList<Point> getPoints() {
        return points;
    }

    private final ObservableList<Connection> connectionsUnmodifiable = FXCollections.unmodifiableObservableList(connections);
    public ObservableList<Connection> getConnectionsUnmodifiable() {
        return connectionsUnmodifiable;
    }

    private final ObservableList<ObservableList<Connection>> connectionsListsUnmodifiable = FXCollections.unmodifiableObservableList(connectionLists);
    public ObservableList<ObservableList<Connection>> getConnectionsListsUnmodifiable() {
        return connectionsListsUnmodifiable;
    }

}
