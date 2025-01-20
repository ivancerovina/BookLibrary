package com.booklibrary.booklibrary.controllers;

import com.booklibrary.booklibrary.datatypes.Author;
import com.booklibrary.booklibrary.datatypes.Book;
import com.booklibrary.booklibrary.datatypes.Member;
import com.booklibrary.booklibrary.dialogs.BookReviewsDialog;
import com.booklibrary.booklibrary.dialogs.CreateBookDialog;
import com.booklibrary.booklibrary.dialogs.CreateMemberDialog;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import static com.booklibrary.booklibrary.utils.Utils.showError;
import static com.booklibrary.booklibrary.utils.Utils.showSuccess;

public class MainController {

    // Tables
    @FXML public TableView<Book> bookTableView;
    @FXML public TableView<Author> authorTableView;
    @FXML public TableView<Member> memberTableView;

    // Book table columns
    @FXML private TableColumn<Book, Number> bookIdColumn;
    @FXML private TableColumn<Book, String> bookTitleColumn;
    @FXML private TableColumn<Book, String> bookAuthorColumn;
    @FXML private TableColumn<Book, String> bookGenreColumn;
    @FXML private TableColumn<Book, Number> bookYearColumn;
    @FXML private TableColumn<Book, String> bookRatingColumn;
    @FXML private TableColumn<Book, String> bookReservedColumn;

    // Author table columns
    @FXML private TableColumn<Author, Number> authorIdColumn;
    @FXML private TableColumn<Author, String> authorNameColumn;
    @FXML private TableColumn<Author, Number> authorBookCountColumn;
    @FXML private TableColumn<Author, String> authorAverageRatingColumn;
    @FXML private TableColumn<Author, String> authorGenresColumn;

    // Member table columns
    @FXML private TableColumn<Member, Number> memberIdColumn;
    @FXML private TableColumn<Member, String> memberNameColumn;
    @FXML private TableColumn<Member, Number> memberReviewNumberColumn;

    // Buttons
    @FXML private Button createBookButton;
    @FXML private Button deleteBookButton;

    @FXML private Button createAuthorButton;
    @FXML private Button deleteAuthorButton;

    @FXML private Button createMemberButton;
    @FXML private Button deleteMemberButton;

    // Lists
    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final ObservableList<Author> authors = FXCollections.observableArrayList();
    private final ObservableList<Member> members = FXCollections.observableArrayList();

    public void initialize() {
        // Set up book table
        bookIdColumn.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
        bookTitleColumn.setCellValueFactory(cellData -> cellData.getValue().getTitleProperty());
        bookAuthorColumn.setCellValueFactory(cellData -> cellData.getValue().getAuthor().getFullNameProperty());
        bookGenreColumn.setCellValueFactory(cellData -> cellData.getValue().getGenreProperty());
        bookYearColumn.setCellValueFactory(cellData -> cellData.getValue().getYearProperty());
        bookRatingColumn.setCellValueFactory(cellData -> {
            var rating = cellData.getValue().getAverageRating();
            var txt = rating != 0 ? rating + " stars" : "No ratings";
            return new SimpleStringProperty(txt);
        });
        bookReservedColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().isReserved()) {
                var mem = cellData.getValue().getReservedByMember();

                if (mem != null) return mem.getFullNameProperty();
            }

            return new SimpleStringProperty("Not reserved");
        });

        // Set up author table
        authorIdColumn.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
        authorNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFullNameProperty());
        authorBookCountColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(Objects.requireNonNull(Book.getByAuthor(cellData.getValue().getIdProperty().get())).size()));
        authorAverageRatingColumn.setCellValueFactory(cellData -> {
            var rating = cellData.getValue().getAverageRating();
            var ratingString = rating != 0 ? rating + " stars" : "No ratings";
            return new SimpleStringProperty(ratingString);
        });
        authorGenresColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getGenres())));

        // Set up member table
        memberIdColumn.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
        memberNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFullNameProperty());
        memberReviewNumberColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getReviews().size()));

        // Add data to tables
        bookTableView.setItems(books);
        authorTableView.setItems(authors);
        memberTableView.setItems(members);

        books.addAll(Book.getBooks());
        authors.addAll(Author.getAuthors());
        members.addAll(Member.getMembers());

        bookTableView.setOnMouseClicked(e -> {
            if (e.getClickCount() > 2) {
                handleBookReviews();
            }
        });
    }

    public void handleCreateBook() {
        var dialog = new CreateBookDialog((Stage) createBookButton.getScene().getWindow());
        dialog.showAndWait();
        refreshBooks();
        refreshAuthors();
    }

    public void handleDeleteBook() {
        var selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            selectedBook.delete();
            refreshBooks();
            showSuccess("Book deleted successfully");
        }
    }

    public void handleCreateAuthor() {
        var dialog = new CreateMemberDialog((Stage) createMemberButton.getScene().getWindow());
        dialog.showAndWait().ifPresent(fullName -> {
            if (!fullName.isEmpty()) {
                System.out.println("Creating author: " + fullName);
                Author.create(fullName);
                refreshAuthors();
            }
        });
    }

    private void handleBookReviews() {
        var book = bookTableView.getSelectionModel().getSelectedItem();
        if (book == null) {
            showError("Please select a book to view reviews");
            return;
        }

        var dialog = new BookReviewsDialog((Stage) createMemberButton.getScene().getWindow(), book);
        dialog.showAndWait();
        refreshAuthors();
        refreshBooks();
        refreshMembers();
    }

    public void handleDeleteAuthor() {
        var selectedAuthor = authorTableView.getSelectionModel().getSelectedItem();
        if (selectedAuthor != null) {
            selectedAuthor.delete();
            refreshAuthors();
            showSuccess("Author deleted successfully");
        }
    }

    public void handleCreateMember() {
        var dialog = new CreateMemberDialog((Stage) createMemberButton.getScene().getWindow());
        dialog.showAndWait().ifPresent(fullName -> {
            if (!fullName.isEmpty()) {
                System.out.println("Creating member: " + fullName);
                Member.create(fullName);
                refreshMembers();
            }
        });
    }

    public void handleDeleteMember() {
        var selectedMember = memberTableView.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            selectedMember.delete();
            refreshMembers();
            showSuccess("Member deleted successfully");
        }
    }

    private void refreshMembers() {
        members.clear();
        members.addAll(Member.getMembers());
    }

    private void refreshBooks() {
        books.clear();
        books.addAll(Book.getBooks());
    }

    private void refreshAuthors() {
        authors.clear();
        authors.addAll(Author.getAuthors());
    }
}