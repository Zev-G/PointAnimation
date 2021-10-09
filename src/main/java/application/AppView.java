package application;

import com.google.gson.Gson;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import json.AnimationJSON;
import json.FrameJSON;
import json.JSONSavable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AppView extends VBox implements JSONSavable<AnimationJSON> {

    public static final Gson GSON = new Gson();
    private static final String STYLE_SHEET = Res.css("app");

    private final ObjectProperty<FrameView> currentFrame = new SimpleObjectProperty<>();
    private final ObservableList<FrameView> frames = FXCollections.observableArrayList();

    private final MenuBar menuBar = new MenuBar();
    private final FramePlayer header = new FramePlayer(this);
    private final FrameEditor frameEditor = new FrameEditor(this);

    private final ObjectProperty<Path> connectedFile = new SimpleObjectProperty<>(null);
    private final IntegerProperty showingAll = new SimpleIntegerProperty(0);

    public AppView(Scene scene) {
        getChildren().addAll(menuBar, header, frameEditor);
        getStyleClass().addAll("app");
        getStylesheets().add(STYLE_SHEET);

        selectFrame(addFrame());
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.Z) {
                getCurrentFrame().takeSnapshot();
                FrameView newFrame = duplicateFrame();
                frames.add(frames.size() - 1, newFrame);
            }
            if (event.getCode().equals(KeyCode.V)) {
                showingAllProperty().set((showingAllProperty().get() + 1) % 3);
                frames.forEach(FrameView::takeSnapshot);
            }
        });

        VBox.setVgrow(frameEditor, Priority.ALWAYS);

        Menu file = new Menu("File");
        MenuItem save = new MenuItem("Save");
        MenuItem saveAs = new MenuItem("Save As");
        MenuItem open = new MenuItem("Open");
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        file.getItems().addAll(save, saveAs, open);
        menuBar.getMenus().add(file);

        save.setOnAction(event -> {
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        saveAs.setOnAction(event -> {
            try {
                saveAs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        open.setOnAction(event -> open());
    }

    public void save() throws IOException {
        if (connectedFile.get() == null) {
            saveAs();
        } else {
            Files.writeString(connectedFile.get(), GSON.toJson(toJSON()));
        }
    }

    public void saveAs() throws IOException {
        FileChooser chooser = new FileChooser();
        if (getScene() != null && getScene().getWindow() != null) {
            File chosen = chooser.showSaveDialog(getScene().getWindow());
            if (chosen != null) {
                connectedFile.set(chosen.toPath());
                save();
            }
        }
    }

    public void open() {
        FileChooser chooser = new FileChooser();
        if (getScene() != null && getScene().getWindow() != null) {
            File chosen = chooser.showOpenDialog(getScene().getWindow());
            if (chosen != null) {
                connectedFile.set(chosen.toPath());
                try {
                    apply(GSON.fromJson(new FileReader(chosen), AnimationJSON.class));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
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
        FrameView newFrame = new FrameView(this);
        addFrame(newFrame);
        return newFrame;
    }
    public void addFrame(FrameView frame) {
        frames.add(frame);
    }

    public static AppView fromJSON(AnimationJSON json, Scene scene) {
        AppView appView = new AppView(scene);
        appView.apply(json);
        return appView;
    }

    @Override
    public void apply(AnimationJSON json) {
        frames.setAll(Arrays.stream(json.frames).map(frame -> FrameView.fromJSON(frame, this)).collect(Collectors.toList()));
        if (!frames.isEmpty()) {
            currentFrame.set(frames.get(frames.size() - 1));
        } else {
            currentFrame.set(null);
        }
    }

    @Override
    public AnimationJSON toJSON() {
        AnimationJSON json = new AnimationJSON();
        json.frames = frames.stream().map(FrameView::toJSON).toArray(FrameJSON[]::new);
        return json;
    }

    public IntegerProperty showingAllProperty() {
        return showingAll;
    }

}
