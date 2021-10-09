package application.shapes;

import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;

import java.util.function.Consumer;

public class SimpleLineType implements LineType {

    public static final SimpleLineType BASIC = new SimpleLineType();

    private final Consumer<LineConnection> lineEditor = line -> {};

    @Override
    public String getName() {
        return "Straight Line";
    }

    @Override
    public PointConnection createNode(Connection connection) {
        LineConnection line = new LineConnection(connection);
        lineEditor.accept(line);
        return line;
    }

}
