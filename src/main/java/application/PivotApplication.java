package application;

import com.me.tmw.debug.devtools.DevScene;
import com.me.tmw.debug.devtools.DevTools;
import com.me.tmw.nodes.util.Layout;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class PivotApplication extends Application {

    public static final double DEFAULT_SIZE = 15;

    @Override
    public void start(Stage primaryStage) {
        DevTools.disableImprovedPerformanceMode();
        AnchorPane root = new AnchorPane();
        DevScene scene = new DevScene(root);
        AppView app = new AppView(scene);
        root.getChildren().add(app);
        Layout.anchor(app);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
