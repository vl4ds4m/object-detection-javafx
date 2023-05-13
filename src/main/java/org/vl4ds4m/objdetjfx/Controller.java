package org.vl4ds4m.objdetjfx;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
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
                        showAlert(Alert.AlertType.WARNING, "Loading an image",
                                "The image resolution should be not greater than 640x640");
                    } else {
                        imageView.setImage(image);
                        visualPane.setMaxHeight(image.getHeight());
                        visualPane.setMaxWidth(image.getWidth());
                        boundedBoxesPane.setMaxHeight(image.getHeight());
                        boundedBoxesPane.setMaxWidth(image.getWidth());
                        boundedBoxesPane.getChildren().clear();
                        objectsCounter.setText("-");
                        isBoundedBoxesDrawn = false;
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Loading an image", "The file isn't an image!");
                }
            } catch (FileNotFoundException e) {
                showAlert(Alert.AlertType.WARNING, "Loading an image", "No image exists!");
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
                    showAlert(Alert.AlertType.WARNING,
                            "Objects detection", "The data file has incorrect data!");
                }
            } else {
                showAlert(Alert.AlertType.WARNING,
                        "Objects detection", "Objects have already been stroked!");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Objects detection", "No image is selected!");
        }
    }

    @FXML
    private void writeLabelsToFile() {
        if (imageView.getImage() != null) {
            if (isBoundedBoxesDrawn) {
                final String labelsFileName = getFileName() + "_labels.txt";
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(labelsFileName))) {
                    for (ObjectData objectData : objectDataList) {
                        writer.write(objectData.X_CENTER() + " " + objectData.Y_CENTER() + " " +
                                objectData.WIDTH() + " " + objectData.HEIGHT() + " " +
                                objectData.confidence() + " " + objectData.type());
                        writer.newLine();
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Writing labels",
                            "Labels have been saved near to the original image!");
                } catch (IOException e) {
                    showAlert(Alert.AlertType.WARNING,
                            "Writing labels", "Can't write labels to the file!");
                }
            } else {
                showAlert(Alert.AlertType.WARNING,
                        "Writing labels", "Stroke objects before writing labels!");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Writing labels", "No image is selected!");
        }
    }

    @FXML
    private void saveProcessedImage() {
        if (imageView.getImage() != null) {
            if (isBoundedBoxesDrawn) {
                SnapshotParameters snapshotParameters = new SnapshotParameters();
                snapshotParameters.setViewport(new Rectangle2D(
                        visualPane.getLayoutX(), visualPane.getLayoutY(),
                        visualPane.getMaxWidth(), visualPane.getMaxHeight()));
                WritableImage writableImage = visualPane.snapshot(snapshotParameters, null);
                File imageFile = new File(getFileName() + "_processed.png");
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null),
                            "png", imageFile);
                    showAlert(Alert.AlertType.INFORMATION, "Saving the processed image",
                            "The image have been saved near to the original image!");
                } catch (IOException e) {
                    showAlert(Alert.AlertType.WARNING,
                            "Saving the processed image", "Can't save the image");
                }
            } else {
                showAlert(Alert.AlertType.WARNING,
                        "Saving the processed image", "Stroke objects before writing labels!");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Saving the processed image", "No image is selected!");
        }
    }

    private void showAlert(Alert.AlertType alertType, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.show();
    }

    private String getFileName() {
        return imageFileName.split("\\.", 2)[0];
    }
}
