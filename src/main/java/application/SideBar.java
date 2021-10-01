package application;

import javafx.scene.layout.BorderPane;

public class SideBar extends BorderPane {

    private static final String STYLE_SHEET = Res.css("side-bar");

    private final FrameEditor editor;

    public SideBar(FrameEditor editor) {
        this.editor = editor;
        getStylesheets().add(STYLE_SHEET);
        getStyleClass().add("side-bar");
        prefHeightProperty().bind(editor.heightProperty());
    }

    public FrameEditor getEditor() {
        return editor;
    }

}
