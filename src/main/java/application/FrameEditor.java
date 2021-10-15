package application;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class FrameEditor extends HBox {

    private static final String STYLE_SHEET = Res.css("frame-editor");

    private final ObjectProperty<FrameView> currentFrame;

    private final SideBar sideBar;
    private final AnchorPane frameHolder = new AnchorPane();

    private final AppView app;

    public FrameEditor(AppView app) {
        this.app = app;
        sideBar = new SideBar(this);
        getChildren().addAll(sideBar, frameHolder);
        getStylesheets().add(STYLE_SHEET);
        getStyleClass().add("frame-editor");
        frameHolder.getStyleClass().add("frame-holder");

        currentFrame = app.currentFrameProperty();
        NodeMisc.runAndAddListener(currentFrame, observable -> {
            frameHolder.getChildren().clear();
            if (currentFrame.get() != null) {
                frameHolder.getChildren().add(currentFrame.get());
                Layout.anchor(currentFrame.get());
            }
        });

        HBox.setHgrow(frameHolder, Priority.ALWAYS);
    }

    public AppView getApp() {
        return app;
    }

}
