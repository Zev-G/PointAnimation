package application.shapes;

public class CurvedLineType implements LineType {

    public static CurvedLineType BASIC = new CurvedLineType();

    @Override
    public String getName() {
        return "Curved Line";
    }

    @Override
    public PointConnection createNode(Connection connection) {
        return new CurveToConnection(connection);
    }

}
