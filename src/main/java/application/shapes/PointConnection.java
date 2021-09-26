package application.shapes;

import javafx.scene.Node;

public interface PointConnection {

    Connection getConnection();
    LineType getLineType();
    Node node();

}
