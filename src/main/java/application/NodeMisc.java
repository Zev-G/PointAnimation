package application;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.css.Styleable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public final class NodeMisc {
    public static final SnapshotParameters TRANSPARENT_SNAPSHOT_PARAMETERS = new SnapshotParameters();

    public NodeMisc() {
    }

    @SafeVarargs
    public static <T> boolean allEqual(T... vals) {
        if (vals.length <= 1) {
            return true;
        } else {
            T first = vals[0];

            for(int i = 1; i < vals.length; ++i) {
                if (vals[i] != first) {
                    return false;
                }
            }

            return true;
        }
    }

    public static Border simpleBorder(Paint paint, double width) {
        return new Border(new BorderStroke[]{new BorderStroke(paint, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(width))});
    }

    public static void addToGridPane(GridPane gridPane, Collection<? extends Node> nodes, IntFunction<Integer> xConverter) {
        addToGridPane(gridPane, nodes, xConverter, 0, (x) -> {
            return 0;
        }, 0);
    }

    public static void addToGridPane(GridPane gridPane, Collection<? extends Node> nodes, IntFunction<Integer> xConverter, int startX, IntFunction<Integer> yConverter, int startY) {
        for(Iterator var6 = nodes.iterator(); var6.hasNext(); startY = (Integer)yConverter.apply(startY)) {
            Node node = (Node)var6.next();
            gridPane.add(node, startX, startY);
            startX = (Integer)xConverter.apply(startX);
        }

    }

    public static String colorToCss(Color color) {
        int red = (int)(color.getRed() * 255.0D);
        int green = (int)(color.getGreen() * 255.0D);
        int blue = (int)(color.getBlue() * 255.0D);
        double opacity = color.getOpacity();
        return "rgba(" + red + ", " + green + ", " + blue + ", " + opacity + ")";
    }

    public static SVGPath svgPath(String s) {
        SVGPath svgPath = new SVGPath();
        svgPath.getStyleClass().addAll(new String[]{"svg-path", "svg"});
        svgPath.setContent(s);
        return svgPath;
    }

    public static Background addToBackground(Background background, BackgroundFill fill) {
        List<BackgroundFill> fills = new ArrayList(background.getFills());
        fills.add(fill);
        return new Background(fills, background.getImages());
    }

    public static Background removeFromBackground(Background background, BackgroundFill fill) {
        List<BackgroundFill> fills = new ArrayList(background.getFills());
        fills.remove(fill);
        return new Background(fills, background.getImages());
    }

    public static ImageView snapshot(Node node) {
        return new ImageView(node.snapshot(TRANSPARENT_SNAPSHOT_PARAMETERS, (WritableImage)null));
    }

    public static Node center(Node inputSVG) {
        VBox wrapper = new VBox(new Node[]{inputSVG});
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    public static Node pad(Node node, Insets pad) {
        VBox padder = new VBox(new Node[]{node});
        padder.setPadding(pad);
        return padder;
    }

    public static Node vGrow(Node input, Priority priority) {
        VBox box = new VBox(new Node[]{input});
        VBox.setVgrow(input, priority);
        return box;
    }

    public static Background simpleBackground(Paint color) {
        return new Background(new BackgroundFill[]{new BackgroundFill((Paint)(color == null ? Color.TRANSPARENT : color), CornerRadii.EMPTY, Insets.EMPTY)});
    }

    public static MenuItem makeMenuItem(String text, EventHandler<ActionEvent> eventHandler) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(eventHandler);
        return item;
    }

    public static Collection<Text> createTexts(Object... objects) {
        List<Text> texts = new ArrayList();
        Font currentFont = null;
        Paint currentFill = null;
        int i = 0;

        for(int objectsLength = objects.length; i < objectsLength; ++i) {
            Object obj = objects[i];
            if (!(obj instanceof String) && !(obj instanceof StringBinding)) {
                if (obj instanceof Text) {
                    texts.add((Text)obj);
                } else if (obj instanceof Paint) {
                    currentFill = (Paint)obj;
                } else {
                    if (!(obj instanceof Font)) {
                        throw new IllegalArgumentException("Invalid argument: " + obj + " at position: " + i + ".");
                    }

                    currentFont = (Font)obj;
                }
            } else {
                Text newText = new Text();
                if (currentFill != null) {
                    newText.setFill(currentFill);
                }

                if (currentFont != null) {
                    newText.setFont(currentFont);
                }

                if (obj instanceof String) {
                    newText.setText(obj.toString());
                } else {
                    newText.textProperty().bind((StringBinding)obj);
                }

                texts.add(newText);
            }
        }

        return texts;
    }

    public static Optional<List<Node>> getChildren(Styleable parent) {
        if (parent instanceof Group) {
            return Optional.of(((Group)parent).getChildren());
        } else if (parent instanceof Pane) {
            return Optional.of(((Pane)parent).getChildren());
        } else if (parent instanceof Tab) {
            return Optional.of(Collections.singletonList(((Tab)parent).getContent()));
        } else if (parent instanceof ScrollPane) {
            return Optional.of(Collections.singletonList(((ScrollPane)parent).getContent()));
        } else if (parent instanceof TitledPane) {
            return Optional.of(Collections.singletonList(((TitledPane)parent).getContent()));
        } else if (parent instanceof ToolBar) {
            return Optional.of(((ToolBar)parent).getItems());
        } else if (parent instanceof SplitPane) {
            return Optional.of(((SplitPane)parent).getItems());
        } else {
            return parent instanceof Labeled ? Optional.of(Collections.singletonList(((Labeled)parent).getGraphic())) : Optional.empty();
        }
    }

    public static void runAndAddListener(Observable observable, InvalidationListener listener) {
        listener.invalidated(observable);
        observable.addListener(listener);
    }

    public static ObjectBinding<Background> backgroundFromProperty(Property<? extends Paint> value) {
        return Bindings.createObjectBinding(() -> {
            return new Background(new BackgroundFill[]{new BackgroundFill((Paint)value.getValue(), CornerRadii.EMPTY, Insets.EMPTY)});
        }, new Observable[]{value});
    }

    static {
        TRANSPARENT_SNAPSHOT_PARAMETERS.setFill(Color.TRANSPARENT);
    }
}
