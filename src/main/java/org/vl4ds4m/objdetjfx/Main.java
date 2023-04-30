package org.vl4ds4m.objdetjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * A main class to launch the application
 */
public class Main extends Application {
    private static final String FXML_PATH = "/properties.fxml";
    private static final String TITLE = "Object Detection Application";

    /**
     * Launch the application
     *
     * @param stage a main frame of the application
     */
    @Override
    public void start(Stage stage) {
        Pane root;

        // Load layout properties from the FXML file
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(FXML_PATH));
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set a scene
        stage.setScene(new Scene(root));
        stage.setMinWidth(root.getPrefWidth());
        stage.setMinHeight(root.getPrefHeight());
        stage.setTitle(TITLE);
        stage.show();

        System.out.println("Hello world!");
    }

    /**
     * Launch the {@code start} method
     *
     * @param args launch parameters
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}