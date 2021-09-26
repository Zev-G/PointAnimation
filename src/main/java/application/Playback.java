package application;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Timer;
import java.util.TimerTask;

public class Playback extends VBox {

    private final Button playStop = new Button("Play");
    private final Slider speed = new Slider(1, 100, 50);
    private final AppView app;
    private final Timer timer = new Timer(true);

    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private int setToLast = -1;
    private final IntegerProperty currentFrame = new SimpleIntegerProperty(-1) {
        @Override
        protected void invalidated() {
            int val = get();
            if (val != -1) {
                if (val >= app.getFrames().size()) {
                    playing.set(false);
                } else {
                    app.currentFrameProperty().set(app.getFrames().get(currentFrame.get()));
                }
            }
        }
    };

    public Playback(AppView app) {
        this.app = app;
        getChildren().addAll(playStop, speed);
        setAlignment(Pos.CENTER);
        playing.addListener(observable -> playStop.setText(playing.get() ? "Stop" : "Play"));
        playStop.setOnAction(event -> playing.set(!playing.get()));

        setMinWidth(100);

        playing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
               currentFrame.set(0);
               startPlaying();
            } else {
                currentFrame.set(-1);
            }
        });
    }

    private void startPlaying() {
        setToLast = 0;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!playing.get()) {
                    timer.purge();
                    cancel();
                } else {
                    if (setToLast++ < app.getFrames().size()) Platform.runLater(() -> currentFrame.set(currentFrame.get() + 1));
                }
            }
        };
        long speed = (long) (101 - this.speed.getValue());
        timer.scheduleAtFixedRate(task, speed, speed);
    }

}
