package com.booklibrary.booklibrary.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CreateAuthorDialog extends Dialog<String> {
    private final TextField fullNameField = new TextField();

    public CreateAuthorDialog(Stage owner) {
        setTitle("Create New Author");
        setHeaderText("Enter author details");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        
        getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        fullNameField.requestFocus();
        
        // Convert the result to string when the create button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return fullNameField.getText();
            }
            return null;
        });
    }
    
    public String getFullName() {
        return fullNameField.getText();
    }
}