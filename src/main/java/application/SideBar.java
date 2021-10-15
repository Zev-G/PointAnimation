package application;

import application.shapes.Point;
import com.me.tmw.nodes.control.svg.SVG;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SideBar extends VBox {

    private static final String STYLE_SHEET = Res.css("side-bar");

    private final FrameEditor editor;
    private final AppView app;

    private final Button clear = new Button("", NodeMisc.svgPath(SVG.resizePath("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z", 1)));
    private final Button delete = new Button("", NodeMisc.svgPath(SVG.resizePath("M 16 9 v 10 H 8 V 9 h 8 m -1.5 -6 h -5 l -1 1 H 5 v 2 h 14 V 4 h -3.5 l -1 -1 z M 18 7 H 6 v 12 c 0 1.1 0.9 2 2 2 h 8 c 1.1 0 2 -0.9 2 -2 V 7 z", 1)));
    private final MenuButton add = new MenuButton("", NodeMisc.svgPath(SVG.resizePath("M 19 13 h -6 v 6 h -2 v -6 H 5 v -2 h 6 V 5 h 2 v 6 h 6 v 2 z", 1)));
    private final HBox selectionButtons = new HBox(clear, delete, add);

    private ListChangeListener<Selectable> currentFrameSelectedListener;

    public SideBar(FrameEditor editor) {
        this.editor = editor;
        this.app = editor.getApp();

        ObservableList<Selectable> selected = FXCollections.observableArrayList();
        BooleanBinding selection = Bindings.isEmpty(selected);

        ChangeListener<FrameView> currentFrameChanged = (observable, oldValue, newValue) -> {
            if (currentFrameSelectedListener != null) {
                oldValue.getSelected().removeListener(currentFrameSelectedListener);
            }
            if (newValue != null) {
                selected.setAll(newValue.getSelected());
                currentFrameSelectedListener = change -> {
                    while (change.next()) {
                        if (change.wasAdded()) {
                            selected.addAll(change.getAddedSubList());
                        }
                        if (change.wasRemoved()) {
                            selected.removeAll(change.getRemoved());
                        }
                    }
                };
                newValue.getSelected().addListener(currentFrameSelectedListener);
            } else {
                selected.clear();
                currentFrameSelectedListener = null;
            }
        };
        currentFrameChanged.changed(app.currentFrameProperty(), null, app.getCurrentFrame());
        app.currentFrameProperty().addListener(currentFrameChanged);

        MenuItem point = new MenuItem("Point");
        point.setOnAction(event -> {
            if (app.getCurrentFrame() != null) {
                Point newPoint = new Point();
                newPoint.setX(25);
                newPoint.setY(25);
                app.getCurrentFrame().getFrameShape().getPoints().add(newPoint);
            }
        });
        add.getItems().add(point);

        clear.disableProperty().bind(selection);
        delete.disableProperty().bind(selection);
        getChildren().addAll(selectionButtons);

        clear.setOnAction(event -> {
            if (app.getCurrentFrame() != null) {
                app.getCurrentFrame().getSelected().clear();
            }
        });
        delete.setOnAction(event -> {
            if (app.getCurrentFrame() != null) {
                app.getCurrentFrame().deleteSelectedItems();
            }
        });

        getStylesheets().add(STYLE_SHEET);
        getStyleClass().add("side-bar");
        selectionButtons.getStyleClass().add("selection-buttons");
        add.getStyleClass().add("button");
        prefHeightProperty().bind(editor.heightProperty());
    }

    public FrameEditor getEditor() {
        return editor;
    }

}
