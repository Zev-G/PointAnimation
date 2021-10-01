package application;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FramePlayer extends HBox {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final String STYLE_SHEET = Res.css("frame-player");

    private final AppView app;

    private final Playback playback;
    private final HBox frames = new HBox();
    private final ScrollPane frameScroller = new ScrollPane(frames);

    private final Map<FrameView, FramePreview> framePreviewMap = new HashMap<>();

    public FramePlayer(AppView app) {
        this.app = app;
        this.playback = new Playback(app);
        getStyleClass().add("frame-player");
        frames.getStyleClass().add("frames");
        getStylesheets().add(STYLE_SHEET);
        getChildren().addAll(playback, new Separator(Orientation.VERTICAL), frameScroller);

        frameScroller.setFitToHeight(true);
        frameScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        app.currentFrameProperty().addListener((observableValue, frameView, t1) -> {
            if (framePreviewMap.containsKey(t1)) {
                framePreviewMap.get(t1).pseudoClassStateChanged(SELECTED, true);
            }
            if (framePreviewMap.containsKey(frameView)) {
                framePreviewMap.get(frameView).pseudoClassStateChanged(SELECTED, false);
            }
        });

        app.getFrames().addListener((ListChangeListener<FrameView>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    int from = c.getFrom();
                    int to = c.getTo();
                    for (int i = to; i < from; i++) {
                        int permutation = c.getPermutation(to);
                        Collections.swap(frames.getChildren(), i, permutation);
                        framePreviewMap.get(app.getFrames().get(permutation)).index.setText(String.valueOf(permutation));
                        framePreviewMap.get(app.getFrames().get(i)).index.setText(String.valueOf(i));
                    }
                } else {
                    if (c.wasAdded()) {
                        for (FrameView view : c.getAddedSubList()) {
                            FramePreview preview = new FramePreview(view);
                            framePreviewMap.put(view, preview);
                            frames.getChildren().add(c.getList().indexOf(view), preview);
                        }
                    }
                    if (c.wasRemoved()) {
                        List<? extends FrameView> removed = c.getRemoved();
                        for (int i = 0, removedSize = removed.size(); i < removedSize; i++) {
                            frames.getChildren().remove(i);
                        }
                    }
                }
            }
            ObservableList<FrameView> appFrames = app.getFrames();
            for (int i = 0, appFramesSize = appFrames.size(); i < appFramesSize; i++) {
                FrameView frame = appFrames.get(i);
                FramePreview preview = framePreviewMap.get(frame);
                if (preview != null) {
                    preview.index.setText(String.valueOf(i + 1));
                }
            }
        });
        app.currentFrameProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) framePreviewMap.get(newV).getStyleClass().add("selected-frame");
            if (oldV != null) framePreviewMap.get(oldV).getStyleClass().remove("selected-frame");
        });

    }

    private class FramePreview extends BorderPane {

        private final ImageView preview = new ImageView();
        private final StackPane imageHolder = new StackPane(preview);
        private final Label index = new Label();

        public FramePreview(FrameView frame) {
            setCenter(imageHolder);
            setBottom(NodeMisc.center(index));
            getStyleClass().add("frame-preview");
            imageHolder.getStyleClass().add("frame-preview-image-holder");
            preview.getStyleClass().add("frame-preview-image");
            index.setAlignment(Pos.CENTER);

//            app.getFrames().addListener((InvalidationListener) observable -> index.setText(String.valueOf(app.getFrames().indexOf(frame))));

            setOnMousePressed(event -> app.currentFrameProperty().set(frame));

            setMaxSize(150, 100);
            setMinSize(150, 100);

            preview.setFitWidth(150);
            preview.setFitHeight(100);

            preview.setSmooth(true);
            preview.setPreserveRatio(true);
            if (frame.lastImageProperty().get() == null) frame.takeSnapshot();
            preview.imageProperty().bind(frame.lastImageProperty());

            index.setText(String.valueOf(app.getFrames().indexOf(frame)));
        }

    }

    public AppView getApp() {
        return app;
    }

}
