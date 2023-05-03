package org.vl4ds4m.objdetjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main extends Application {
    private static final String FXML_PATH = "/properties.fxml";
    private static final String TITLE = "Object Detection Application";
    private final StackPane stackPane = new StackPane();
    private final ImageView imageView = new ImageView();
    private final Pane rectanglesPane = new Pane();
    private boolean isRectanglesDrawn = false;
    private final Button imageAdditionButton = new Button();
    private final Button objectsDetectionButton = new Button();

    private GridPane loadRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(FXML_PATH));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleImageLoader(Stage stage) {
        imageAdditionButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File imageFile = fileChooser.showOpenDialog(stage);
            if (imageFile != null) {
                try {
                    Image image = new Image(new FileInputStream(imageFile.getAbsolutePath()));
                    if (image.isError()) {
                        showAlert("Can't open the file.", "The file isn't an image!");
                    } else {
                        imageView.setImage(image);
                        rectanglesPane.getChildren().clear();
                        isRectanglesDrawn = false;
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void handleObjectsStroking() {
        objectsDetectionButton.setOnAction(actionEvent -> {
            if (imageView.getImage() != null) {
                if (!isRectanglesDrawn) {
                    Rectangle rectangle = new Rectangle();
                    rectangle.setX(10.0);
                    rectangle.setY(100.0);
                    rectangle.setWidth(200.0);
                    rectangle.setHeight(200.0);
                    rectangle.setFill(Color.TRANSPARENT);
                    rectangle.setStroke(Color.RED);
                    rectangle.setStrokeWidth(3.0);
                    rectanglesPane.getChildren().add(rectangle);
                    isRectanglesDrawn = true;
                } else {
                    showAlert("Stroking objects", "Objects have already been stroked!");
                }
            } else {
                showAlert("Can't stroke objects.", "No image is selected!");
            }
        });
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    @Override
    public void start(Stage stage) {
        // Create layers
        GridPane root = loadRootLayout();
        VBox buttonsBox = new VBox();
        Label imageLabel = new Label("Your image");

        // Adjust scene elements
        imageAdditionButton.setText("Load an image");
        objectsDetectionButton.setText("Stroke objects");
        imageLabel.setFont(Font.font(20.0));
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setSpacing(10.0);

        stackPane.getChildren().addAll(imageLabel, imageView, rectanglesPane);
        buttonsBox.getChildren().addAll(imageAdditionButton, objectsDetectionButton);
        root.add(stackPane, 0, 0);
        root.add(buttonsBox, 1, 0);

        // Launch handlers
        handleImageLoader(stage);
        handleObjectsStroking();

        // Set a scene
        stage.setScene(new Scene(root));
        stage.setMinWidth(root.getPrefWidth());
        stage.setMinHeight(root.getPrefHeight());
        stage.setTitle(TITLE);

        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}