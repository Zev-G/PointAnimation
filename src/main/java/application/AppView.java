package application;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AppView extends VBox {

    private static final String STYLE_SHEET = Res.css("app");

    private final ObjectProperty<FrameView> currentFrame = new SimpleObjectProperty<>();
    private final ObservableList<FrameView> frames = FXCollections.observableArrayList();

    private final FramePlayer header = new FramePlayer(this);
    private final FrameEditor frameEditor = new FrameEditor(this);

    public AppView(Scene scene) {
        getChildren().addAll(header, frameEditor);
        getStyleClass().addAll("app");
        getStylesheets().add(STYLE_SHEET);

        selectFrame(addFrame());
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.Z) {
                getCurrentFrame().takeSnapshot();
                FrameView newFrame = duplicateFrame();
                frames.add(frames.size() - 1, newFrame);
            }
        });

        VBox.setVgrow(frameEditor, Priority.ALWAYS);
    }

    public ObjectProperty<FrameView> currentFrameProperty() {
        return currentFrame;
    }
    public FrameView getCurrentFrame() {
        return currentFrame.get();
    }

    public ObservableList<FrameView> getFrames() {
        return frames;
    }

    public void selectFrame(FrameView frame) {
        currentFrame.set(frame);
    }

    public FrameView duplicateFrame() {
        return getCurrentFrame().duplicate();
    }

    public FrameView addFrame() {
        FrameView newFrame = new FrameView();
        addFrame(newFrame);
        return newFrame;
    }
    public void addFrame(FrameView frame) {
        frames.add(frame);
    }

}
