package application.shapes;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import util.FXLists;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Shape extends Parent {

    private final ObservableList<Point> points = FXCollections.observableArrayList();
    private final ObservableList<ObservableList<Connection>> connectionLists = FXLists.map(points, Point::getConnectionsTo);
    private final ObservableList<Connection> connections = FXLists.reduce(connectionLists);

    private final Map<Connection, Node> connectionNodeMap = new HashMap<>();

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
                getChildren().addAll(0, c.getAddedSubList().stream()
                        .map(connection -> connectionNodeMap.computeIfAbsent(connection, connection1 -> connection1.createNode().node()))
                        .collect(Collectors.toList()));
                getChildren().removeAll(c.getRemoved().stream()
                        .map(connectionNodeMap::get)
                        .collect(Collectors.toList()));
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
            Point.connect(oldToNewPoints.get(connection.getStart()), oldToNewPoints.get(connection.getEnd()), connection.getConnector());
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
