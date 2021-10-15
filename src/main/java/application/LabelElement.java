package application;

import application.shapes.Point;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class LabelElement extends BorderPane implements Selectable {

    private final BooleanProperty selected = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED, selected.get());
        }
    };

    private final Point locationController = new Point();

    private final BooleanProperty draggable = new SimpleBooleanProperty(true);
    private final double[] dragData = new double[4];
    private boolean dragging = false;

    private final Label label = new Label("Filler Text");

    public LabelElement() {
        setCenter(label);
        label.setMouseTransparent(true);
        setPickOnBounds(true);
        getStyleClass().add("label-element");

        layoutXProperty().bind(locationController.xProperty());
        layoutYProperty().bind(locationController.yProperty());

        setOnMousePressed(event -> {
            requestFocus();
            if (event.getButton() == MouseButton.PRIMARY && !event.isShiftDown()) {
                dragData[0] = locationController.getX();
                dragData[1] = locationController.getY();
                dragData[2] = event.getScreenX();
                dragData[3] = event.getScreenY();
                dragging = true;
            }
        });
        setOnMouseDragged(event -> {
            if (dragging && draggable.get()) {
                locationController.setX(event.getScreenX() - dragData[2] + dragData[0]);
                locationController.setY(event.getScreenY() - dragData[3] + dragData[1]);
            }
        });
        setOnMouseReleased(event -> dragging = false);
    }

    @Override
    public Point[] getPoints() {
        return new Point[]{ locationController };
    }

    @Override
    public void remove(FrameView view) {
        view.getChildren().remove(this);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public Point getLocationController() {
        return locationController;
    }

}
