package application;

import com.me.tmw.debug.devtools.DevScene;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class PivotApplication extends Application {

    public static final double DEFAULT_SIZE = 15;

    @Override
    public void start(Stage primaryStage) {
        AnchorPane root = new AnchorPane();
        Scene scene = new DevScene(root);
        AppView app = new AppView(scene);
        root.getChildren().add(app);
        Layout.anchor(app);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
