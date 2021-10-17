package application;

import application.shapes.Point;
import com.me.tmw.debug.devtools.inspectors.InspectorBase;
import com.me.tmw.nodes.util.Layout;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.Map;

public class SideBar extends VBox {

    private static final String STYLE_SHEET = Res.css("side-bar");

    private final FrameEditor editor;
    private final AppView app;

    private final Button clear = new Button("", NodeMisc.svgPath("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z"));
    private final Button delete = new Button("", NodeMisc.svgPath("M 16 9 v 10 H 8 V 9 h 8 m -1.5 -6 h -5 l -1 1 H 5 v 2 h 14 V 4 h -3.5 l -1 -1 z M 18 7 H 6 v 12 c 0 1.1 0.9 2 2 2 h 8 c 1.1 0 2 -0.9 2 -2 V 7 z"));
    private final MenuButton add = new MenuButton("", NodeMisc.svgPath("M 19 13 h -6 v 6 h -2 v -6 H 5 v -2 h 6 V 5 h 2 v 6 h 6 v 2 z"));
    private final HBox selectionButtons = new HBox(clear, delete, add);

    private ListChangeListener<Selectable> currentFrameSelectedListener;

    private final ObservableList<Selectable> selected = FXCollections.observableArrayList();
    private final ListView<Selectable> selectedView = new ListView<>();
    private final Accordion editSelectables = new Accordion();
    private final ScrollPane editorScrollPane = new ScrollPane(editSelectables);
    private final Map<Selectable, SelectableEditor> selectableEditorMap = new HashMap<>();

    private final InspectorBase inspector = new InspectorBase() {

        private final Label type = new Label();

        {
            usingCSS = false;
            usingOverlay = true;
            getPopupContent().getChildren().add(type);
        }

        @Override
        protected void populatePopup(Node node) {
            type.setText(node.getClass().getSimpleName());
            overlayPopupContent.minWidthProperty().bind(Bindings.createDoubleBinding(() -> node.getBoundsInParent().getWidth(), node.boundsInParentProperty()));
            overlayPopupContent.minHeightProperty().bind(Bindings.createDoubleBinding(() -> node.getBoundsInParent().getHeight(), node.boundsInParentProperty()));
        }

        @Override
        protected void layoutPopup(Node node) {
            Bounds boundsOnScreen = Layout.nodeOnScreen(node);
            if (boundsOnScreen != null) {
                getPopup().setX(boundsOnScreen.getMinX());
                getPopup().setY(boundsOnScreen.getMaxY());

                getOverlayPopup().setX(boundsOnScreen.getMinX());
                getOverlayPopup().setY(boundsOnScreen.getMinY());
            }
        }
    };
    private final Callback<ListView<Selectable>, ListCell<Selectable>> selectedCellFactory = listView -> new ListCell<>() {
        {
            setOnMouseEntered(event -> {
                Selectable item = getItem();
                if (item instanceof Node) {
                    inspector.setExamined((Node) item);
                }
            });
            setOnMouseExited(event -> inspector.setExamined(null));
        }

        @Override
        protected void updateItem(Selectable item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setText(item.getClass().getSimpleName());
                if (!getStyleClass().contains("real-tree-cell")) {
                    getStyleClass().add("real-tree-cell");
                }
            } else {
                setGraphic(null);
                setText(null);
                getStyleClass().remove("real-tree-cell");
            }
        }
    };
    private final ChangeListener<FrameView> currentFrameChanged = (observable, oldValue, newValue) -> {
        if (currentFrameSelectedListener != null) {
            oldValue.getSelected().removeListener(currentFrameSelectedListener);
        }
        if (newValue != null) {
            selected.setAll(newValue.getSelected());
            selectedView.getItems().setAll(newValue.getSelected());
            currentFrameSelectedListener = change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        selected.addAll(change.getAddedSubList());
                        selectedView.getItems().addAll(change.getAddedSubList());
                    }
                    if (change.wasRemoved()) {
                        selected.removeAll(change.getRemoved());
                        selectedView.getItems().removeAll(change.getRemoved());
                    }
                }
            };
            newValue.getSelected().addListener(currentFrameSelectedListener);
        } else {
            selected.clear();
            selectedView.getItems().clear();
            currentFrameSelectedListener = null;
        }
    };

    public SideBar(FrameEditor editor) {
        this.editor = editor;
        this.app = editor.getApp();
        getStylesheets().add(STYLE_SHEET);
        getStyleClass().add("side-bar");
        selectionButtons.getStyleClass().add("selection-buttons");
        add.getStyleClass().add("button");


        selectedView.setMaxHeight(250);
        selectedView.prefHeightProperty().bind(Bindings.size(selected).multiply(23).add(23));
        selectedView.minHeightProperty().bind(Bindings.min(250, selectedView.prefHeightProperty()));
        VBox.setVgrow(editorScrollPane, Priority.SOMETIMES);
        editorScrollPane.setFitToWidth(true);

        selected.addListener((ListChangeListener<? super Selectable>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Selectable added : c.getAddedSubList()) {
                        if (!selectableEditorMap.containsKey(added)) {
                            selectableEditorMap.put(added, new SelectableEditor(added));
                        }
                        if (!editSelectables.getPanes().contains(selectableEditorMap.get(added))) {
                            editSelectables.getPanes().add(selectableEditorMap.get(added));
                        }
                    }
                }
                if (c.wasRemoved()) {
                    for (Selectable removed : c.getRemoved()) {
                        if (selectableEditorMap.containsKey(removed)) {
                            editSelectables.getPanes().remove(selectableEditorMap.get(removed));
                        }
                    }
                }
            }
        });
        selectedView.getSelectionModel().selectedItemProperty().addListener(observable -> {
            Selectable selected = selectedView.getSelectionModel().getSelectedItem();
            TitledPane expanded = editSelectables.getExpandedPane();
            if (selected == null) {
                editSelectables.setExpandedPane(null);
            } else if (expanded == null || (expanded instanceof SelectableEditor && ((SelectableEditor) expanded).selectable != selected)) {
                editSelectables.setExpandedPane(selectableEditorMap.getOrDefault(selected, null));
            }
        });
        selectedView.getItems().addListener((InvalidationListener) observable -> {
            if (selectedView.getItems().size() == 1) {
                selectedView.getSelectionModel().select(selectedView.getItems().get(0));
            }
        });

        selectedView.setCellFactory(selectedCellFactory);

        BooleanBinding selection = Bindings.isEmpty(selected);

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
        MenuItem label = new MenuItem("Label");
        label.setOnAction(event -> {
            if (app.getCurrentFrame() != null) {
                LabelElement newElement = new LabelElement();
                newElement.getLocationController().setX(25);
                newElement.getLocationController().setY(25);
                app.getCurrentFrame().getChildren().add(newElement);
            }
        });
        add.getItems().addAll(point, label);

        clear.disableProperty().bind(selection);
        delete.disableProperty().bind(selection);

        TitledPane selectedTitledPane = new TitledPane("Selected", selectedView);
        selectedTitledPane.getStyleClass().add("selectable-editor");

        getChildren().addAll(selectionButtons, selectedTitledPane, editorScrollPane);

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
    }

    public FrameEditor getEditor() {
        return editor;
    }

    private class SelectableEditor extends TitledPane {

        private final Selectable selectable;
        private boolean loaded = false;

        private final VBox content = new VBox();

        public SelectableEditor(Selectable selectable) {
            this.selectable = selectable;
            getStyleClass().add("selectable-editor");
            setText(selectable.getClass().getSimpleName());
            setContent(content);
            setExpanded(false);
            setAnimated(false);

            expandedProperty().addListener(observable -> {
                boolean expanded = isExpanded();
                if (expanded && selectedView.getSelectionModel().getSelectedItem() != selectable) {
                    selectedView.getSelectionModel().select(selectable);
                }
            });

            expandedProperty().addListener(observable -> {
                if (!loaded && isExpanded()) {
                    load();
                }
            });
        }

        private void load() {
            this.loaded = true;
            for (EditableProperty<?> property : this.selectable.getEditableProperties()) {
                content.getChildren().add(new EditablePropertyView<>(property));
            }
        }

        public Selectable getSelectable() {
            return selectable;
        }

    }

    private static class EditablePropertyView<T> extends TitledPane {

        private final EditableProperty<T> editableProperty;

        public EditablePropertyView(EditableProperty<T> editableProperty) {
            super(editableProperty.getName(), editableProperty.getEditor());
            getStyleClass().add("editable-property-view");
            this.editableProperty = editableProperty;
        }

    }

}
