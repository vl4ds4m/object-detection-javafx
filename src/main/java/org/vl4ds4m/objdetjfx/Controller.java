package org.vl4ds4m.objdetjfx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

/**
 * This class manage the application.
 * <p>
 * Its name is defined in the fxml file, which contains
 * the properties of class fields and the ways to call class methods.
 */
public class Controller {
    @FXML
    private Button imageAdditionButton;

    @FXML
    private void initialize() {
    }

    @FXML
    private void clickOnImageAdditionButton() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Loading an image");
        alert.setContentText("No images to load!");
        alert.showAndWait();
    }
}
