package org.vl4ds4m.objdetjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    private static final String FXML_PATH = "/scene_structure.fxml";
    private static final String TITLE = "Object Detection Application";

    @Override
    public void start(Stage stage) throws Exception {
        VBox root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(FXML_PATH)));

        stage.setScene(new Scene(root));
        stage.setMinWidth(root.getMinWidth());
        stage.setMinHeight(root.getMinHeight());
        stage.setTitle(TITLE);

        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}