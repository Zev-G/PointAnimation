package application;

import javafx.scene.layout.BorderPane;

public class SideBar extends BorderPane {

    private final FrameEditor editor;

    public SideBar(FrameEditor editor) {
        this.editor = editor;
    }

    public FrameEditor getEditor() {
        return editor;
    }

}
