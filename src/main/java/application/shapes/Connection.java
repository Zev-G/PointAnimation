package application.shapes;

public class Connection {

    private final Point start;
    private final Point end;
    private final LineType connector;

    public Connection(Point start, Point b, LineType connector) {
        this.start = start;
        this.end = b;
        this.connector = connector;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public LineType getConnector() {
        return connector;
    }

    public PointConnection createNode() {
        return connector.createNode(this);
    }

    public void remove() {
        start.getConnectionsTo().remove(this);
        end.getConnectionsFrom().remove(this);
    }

}
