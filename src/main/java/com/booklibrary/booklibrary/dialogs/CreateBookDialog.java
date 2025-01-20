package com.booklibrary.booklibrary.dialogs;

import com.booklibrary.booklibrary.datatypes.Author;
import com.booklibrary.booklibrary.datatypes.Book;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.Year;

import static com.booklibrary.booklibrary.utils.Utils.showError;

public class CreateBookDialog extends Dialog<Book> {
    private final TextField titleField = new TextField();
    private final ComboBox<Author> authorComboBox = new ComboBox<>();
    private final TextField genreField = new TextField();
    private final Spinner<Integer> yearSpinner;

    public CreateBookDialog(Stage owner) {
        setTitle("Create New Book");
        setHeaderText("Enter book details");

        // Initialize year spinner with current year as max
        int currentYear = Year.now().getValue();
        yearSpinner = new Spinner<>(1000, currentYear, currentYear);
        yearSpinner.setEditable(true);

        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Add form fields
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Author:"), 0, 1);
        grid.add(authorComboBox, 1, 1);

        grid.add(new Label("Genre:"), 0, 2);
        grid.add(genreField, 1, 2);

        grid.add(new Label("Year:"), 0, 3);
        grid.add(yearSpinner, 1, 3);

        getDialogPane().setContent(grid);

        // Request focus on the title field by default
        titleField.requestFocus();

        // Load authors into combo box
        loadAuthors();

        // Convert the result to a Book object when the create button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                var title = getBookTitle();
                var author = getSelectedAuthor();
                var genre = getGenre();
                var year = getYear();

                if (title.isEmpty() || author == null || genre.isEmpty()) {
                    showError("Please fill in all fields");
                    return null;
                }

                Book.create(title, author.getIdProperty().get(), genre, year);
            }
            return null;
        });
    }

    // This method should be implemented to load authors from your data source
    private void loadAuthors() {
        // TODO: Replace with actual author loading logic
        ObservableList<Author> authors = FXCollections.observableArrayList(Author.getAuthors());
        // Add authors to the list
        authorComboBox.setItems(authors);
        authorComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Author author, boolean empty) {
                super.updateItem(author, empty);
                if (empty || author == null) {
                    setText(null);
                    setHeaderText(null);
                } else {
                    setText(author.getFullNameProperty().get());
                }
            }
        });

    }

    // Getter methods for form fields
    public String getBookTitle() {
        return titleField.getText();
    }

    public Author getSelectedAuthor() {
        return authorComboBox.getValue();
    }

    public String getGenre() {
        return genreField.getText();
    }

    public Integer getYear() {
        return yearSpinner.getValue();
    }
}