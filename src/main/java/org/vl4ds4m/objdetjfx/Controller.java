package org.vl4ds4m.objdetjfx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.List;

/**
 * This class manage the application.
 * <p>
 * Its name is defined in the fxml file, which contains
 * the properties of class fields and the ways to call class methods.
 */
public class Controller {
    @FXML
    private StackPane visualPane;
    @FXML
    private ImageView imageView;
    @FXML
    private Pane boundedBoxesPane;
    @FXML
    private Label objectsCounter;
    private String imageFileName = "";
    private boolean isBoundedBoxesDrawn = false;
    private List<ObjectData> objectDataList = List.of();
    private static final String OUT_FILE_SUFFIX = "_labels.txt";

    @FXML
    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        File imageFile = fileChooser.showOpenDialog(visualPane.getScene().getWindow());
        if (imageFile != null) {
            imageFileName = imageFile.getAbsolutePath();
            try {
                Image image = new Image(new FileInputStream(imageFileName));
                if (!image.isError()) {
                    if (Double.max(image.getWidth(), image.getHeight()) > 640.0) {
                        showAlert("Loading an image",
                                "The image resolution should be not greater than 640x640");
                    } else {
                        imageView.setImage(image);
                        boundedBoxesPane.setMaxHeight(image.getHeight());
                        boundedBoxesPane.setMaxWidth(image.getWidth());
                        boundedBoxesPane.getChildren().clear();
                        objectsCounter.setText("-");
                        isBoundedBoxesDrawn = false;
                    }
                } else {
                    showAlert("Loading an image", "The file isn't an image!");
                }
            } catch (FileNotFoundException e) {
                showAlert("Loading an image", "No image exists!");
            }
        }
    }

    @FXML
    private void detectObjects() {
        if (imageView.getImage() != null) {
            if (!isBoundedBoxesDrawn) {
                objectDataList = Detector.detectObjectsOnImage(imageFileName);
                try {
                    objectDataList.forEach(data -> {
                        Rectangle rectangle = new Rectangle(
                                data.X_CENTER() - data.WIDTH() / 2,
                                data.Y_CENTER() - data.HEIGHT() / 2,
                                data.WIDTH(), data.HEIGHT());
                        rectangle.setFill(Color.TRANSPARENT);
                        rectangle.setStroke(Color.RED);
                        rectangle.setStrokeWidth(3.0);
                        boundedBoxesPane.getChildren().add(rectangle);
                    });
                    Rectangle rectangle = new Rectangle(0.0, 0.0,
                            boundedBoxesPane.getMaxWidth(),
                            boundedBoxesPane.getMaxHeight());
                    rectangle.setFill(Color.TRANSPARENT);
                    rectangle.setStroke(Color.BLACK);
                    rectangle.setStrokeWidth(3.0);
                    boundedBoxesPane.getChildren().add(rectangle);
                    objectsCounter.setText(String.valueOf(objectDataList.size()));
                    isBoundedBoxesDrawn = true;
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    showAlert("Objects detection", "The data file has incorrect data!");
                }
            } else {
                showAlert("Objects detection", "Objects have already been stroked!");
            }
        } else {
            showAlert("Objects detection", "No image is selected!");
        }
    }

    @FXML
    private void writeLabelsToFile() {
        if (imageView.getImage() != null) {
            if (isBoundedBoxesDrawn) {
                final String labelsFileName = imageFileName.split("\\.", 2)[0] + OUT_FILE_SUFFIX;
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(labelsFileName))) {
                    for (ObjectData objectData : objectDataList) {
                        writer.write(objectData.X_CENTER() + " " +
                                objectData.Y_CENTER() + " " +
                                objectData.WIDTH() + " " +
                                objectData.HEIGHT() + " " +
                                objectData.confidence());
                        writer.newLine();
                    }
                } catch (IOException e) {
                    showAlert("Writing labels", "Can't write labels to the file!");
                }
            } else {
                showAlert("Writing labels", "Stroke objects before writing labels!");
            }
        } else {
            showAlert("Writing labels", "No image is selected!");
        }
    }

    private void showAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }
}
