package com.booklibrary.booklibrary.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public final class Utils {
    private Utils() {
    }

    public static Integer safelyParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void showError(String message) {
        var alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    public static void showSuccess(String message) {
        var alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    public static StackPane getFxml(String name) {
        var loader = new FXMLLoader(Utils.class.getResource("com.myapp.app/" + name + ".fxml"));

        try {
            return loader.load();
        } catch (IOException e) {
            return null;
        }
    }
}
