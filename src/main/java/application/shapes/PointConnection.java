package application.shapes;

import javafx.scene.Node;

import java.util.Collections;
import java.util.List;

public interface PointConnection {

    Connection getConnection();
    LineType getLineType();
    Node node();

    default List<ControlPoint> getControlPoints() { return Collections.emptyList(); }

}
