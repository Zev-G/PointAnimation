package application;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class Layout {
    public static void anchor(Node node) {
        AnchorPane.setLeftAnchor(node, 0D);
        AnchorPane.setRightAnchor(node, 0D);
        AnchorPane.setTopAnchor(node, 0D);
        AnchorPane.setBottomAnchor(node, 0D);
    }
}
