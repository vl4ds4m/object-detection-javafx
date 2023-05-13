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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private Label smallVehiclesCounter;
    @FXML
    private Label largeVehiclesCounter;
    @FXML
    private Label planesCounter;
    @FXML
    private Label helicoptersCounter;
    @FXML
    private Label shipsCounter;
    @FXML
    private Label totalCounter;
    private String imageFileName = "";
    private boolean isBoundedBoxesDrawn = false;
    private List<ObjectData> objectDataList = List.of();
    private Map<ObjectData.Type, Label> labelCountersMap = Map.of();

    @FXML
    private void initialize() {
        labelCountersMap = Map.of(
                ObjectData.Type.SMALL_VEHICLE, smallVehiclesCounter,
                ObjectData.Type.LARGE_VEHICLE, largeVehiclesCounter,
                ObjectData.Type.PLANE, planesCounter,
                ObjectData.Type.HELICOPTER, helicoptersCounter,
                ObjectData.Type.SHIP, shipsCounter);
    }

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
                        visualPane.setMaxWidth(image.getWidth());
                        visualPane.setMaxHeight(image.getHeight());
                        boundedBoxesPane.setMaxWidth(image.getWidth());
                        boundedBoxesPane.setMaxHeight(image.getHeight());
                        boundedBoxesPane.getChildren().clear();
                        labelCountersMap.forEach((key, label) -> label.setText("-"));
                        totalCounter.setText("-");
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
                List<Integer> objectsCountersList = new ArrayList<>(
                        Collections.nCopies(ObjectData.Type.values().length, 0));
                try {
                    objectDataList.forEach(data -> {
                        Rectangle rectangle = new Rectangle(
                                data.X_CENTER() - data.WIDTH() / 2,
                                data.Y_CENTER() - data.HEIGHT() / 2,
                                data.WIDTH(), data.HEIGHT());
                        rectangle.setFill(Color.TRANSPARENT);
                        rectangle.setStroke(data.type().toColor());
                        rectangle.setStrokeWidth(2.0);
                        boundedBoxesPane.getChildren().add(rectangle);
                        objectsCountersList.set(data.type().toNum(), objectsCountersList.get(data.type().toNum()) + 1);
                    });
                    Rectangle rectangle = new Rectangle(0.0, 0.0,
                            boundedBoxesPane.getWidth(),
                            boundedBoxesPane.getHeight());
                    rectangle.setFill(Color.TRANSPARENT);
                    rectangle.setStroke(Color.BLACK);
                    rectangle.setStrokeWidth(3.0);
                    boundedBoxesPane.getChildren().add(rectangle);
                    labelCountersMap.forEach(
                            (key, label) -> label.setText(objectsCountersList.get(key.toNum()).toString()));
                    totalCounter.setText(Integer.toString(objectDataList.size()));
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
                try (BufferedWriter writer =
                             new BufferedWriter(new FileWriter(getNewFile(imageFileName) + "_labels.txt"))) {
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
                try {
                    File imageFile = new File(getNewFile(imageFileName) + "_processed.png");
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

    private String getNewFile(String string) throws IOException {
        Path filePath = Path.of(string);
        Path newDirectory = Path.of(filePath.getParent().toString(), "ObjectDetectionApp");
        if (!newDirectory.toFile().exists()) {
            Files.createDirectory(newDirectory);
        }
        return newDirectory + File.separator +  filePath.getFileName().toString().split("\\.", 2)[0];
    }
}
