package com.booklibrary.booklibrary.dialogs;

import com.booklibrary.booklibrary.datatypes.Book;
import com.booklibrary.booklibrary.datatypes.BookReview;
import com.booklibrary.booklibrary.datatypes.Member;
import com.booklibrary.booklibrary.datatypes.ReservationRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class BookManagementDialog extends Dialog<BookReview> {
    private static final String STAR = "★";
    private static final String EMPTY_STAR = "☆";
    private final VBox reviewsContainer = new VBox(10);
    private final TextArea newReviewText = new TextArea();
    private final ComboBox<Integer> starRatingPicker = new ComboBox<>();
    private final ComboBox<Member> memberPicker = new ComboBox<>();
    private final ComboBox<Member> reservationMemberPicker = new ComboBox<>();
    private final ObservableList<BookReview> reviews = FXCollections.observableArrayList();
    private Book book;
    private Label reservationStatus;

    public BookManagementDialog(Stage owner, Book book) {
        this.book = book;

        setTitle(book.getTitleProperty().get() + " by " + book.getAuthor().getFullNameProperty().get());
        setHeaderText(null);

        DialogPane dialogPane = getDialogPane();
        dialogPane.setPrefWidth(500);
        dialogPane.setPrefHeight(600);

        // Create tab pane
        TabPane tabPane = new TabPane();

        // Reviews tab
        Tab reviewsTab = new Tab("Reviews");
        reviewsTab.setContent(createReviewsContent());
        reviewsTab.setClosable(false);

        // Reservations tab
        Tab reservationsTab = new Tab("Reservations");
        reservationsTab.setContent(createReservationsContent());
        reservationsTab.setClosable(false);

        Tab historyTab = new Tab("Reservation History");
        historyTab.setContent(createReservationHistoryContent());
        historyTab.setClosable(false);

        tabPane.getTabs().addAll(reviewsTab, reservationsTab, historyTab);

        // Set up dialog buttons
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeButton);

        dialogPane.setContent(tabPane);

        // Load initial data
        loadReviews();
        loadMembers();
    }

    private VBox createReviewsContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        HBox averageRatingBox = createAverageRatingDisplay();
        mainLayout.getChildren().add(averageRatingBox);

        ScrollPane scrollPane = new ScrollPane(reviewsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(300);
        mainLayout.getChildren().add(scrollPane);

        VBox newReviewSection = createNewReviewSection();
        mainLayout.getChildren().add(newReviewSection);

        return mainLayout;
    }

    private VBox createReservationsContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        // Current reservation status
        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER);
        Label statusLabel = new Label("Current Status");
        statusLabel.getStyleClass().add("section-title");

        reservationStatus = new Label();
        var reservedBy = book.getReservedByMember();
        if (reservedBy != null) {
            reservationStatus.setText("Reserved by: " + reservedBy.getFullNameProperty().get());
        } else {
            reservationStatus.setText("Not currently reserved");
        }

        statusBox.getChildren().addAll(statusLabel, reservationStatus);

        // Reservation controls
        VBox controlsBox = new VBox(10);
        controlsBox.setAlignment(Pos.CENTER);

        Label memberLabel = new Label("Select Member");
        reservationMemberPicker.setPromptText("Choose member");
        reservationMemberPicker.setPrefWidth(200);

        Button reserveButton = new Button("Reserve Book");
        reserveButton.setOnAction(e -> handleReservation());

        Button removeReservationButton = new Button("Remove Reservation");
        removeReservationButton.setOnAction(e -> removeReservation());
        removeReservationButton.setDisable(book.getReservedByProperty() == null);

        controlsBox.getChildren().addAll(memberLabel, reservationMemberPicker, new HBox(10, reserveButton, removeReservationButton));

        container.getChildren().addAll(statusBox, new Separator(), controlsBox);
        return container;
    }

    private void handleReservation() {
        Member selectedMember = reservationMemberPicker.getValue();
        if (selectedMember == null) {
            showAlert("Please select a member to make the reservation.");
            return;
        }

        if (book.getReservedByMember() != null) {
            showAlert("This book is already reserved.");
            return;
        }

        // Update the book's reservation
        book.reserve(selectedMember.getIdProperty().get());

        // Log the reservation in reservation_records
        ReservationRecord.create(
                selectedMember.getIdProperty().get(),
                book.getIdProperty().get(),
                new java.sql.Timestamp(System.currentTimeMillis())
        );

        // Reset the member picker and refresh the dialog
        reservationMemberPicker.setValue(null);
        updateReservationStatus();
    }

    private void removeReservation() {
        if (book.getReservedByMember() == null) {
            showAlert("This book is not currently reserved.");
            return;
        }

        // Find the active reservation record and mark it as returned
        var records = ReservationRecord.getByBook(book.getIdProperty().get());
        for (ReservationRecord record : records) {
            if (!record.isReturned()) {
                record.markAsReturned();
                break;
            }
        }

        // Remove the reservation
        book.reserve(null);
        reservationStatus.setText("Not currently reserved");

        updateReservationStatus();
    }

    private void updateReservationStatus() {
        this.book = Book.findById(book.getIdProperty().get());
        // Refresh the dialog to show updated reservation status
        DialogPane dialogPane = getDialogPane();
        TabPane tabPane = (TabPane) dialogPane.getContent();

        // Update reservations tab
        Tab reservationsTab = tabPane.getTabs().get(1);
        reservationsTab.setContent(createReservationsContent());

        // Update history tab
        Tab historyTab = tabPane.getTabs().get(2);
        historyTab.setContent(createReservationHistoryContent());
    }

    private HBox createAverageRatingDisplay() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER);

        double averageRating = calculateAverageRating();
        String ratingText = reviews.isEmpty() ? "No ratings yet" : String.format("%.1f/5.0 (%d reviews)", averageRating, reviews.size());

        Label ratingLabel = new Label(ratingText);
        ratingLabel.getStyleClass().add("average-rating-label");

        Label starsLabel = new Label(getStarDisplay((int) Math.round(averageRating)));
        starsLabel.getStyleClass().add("stars-label");

        container.getChildren().addAll(ratingLabel, starsLabel);
        return container;
    }

    private VBox createNewReviewSection() {
        VBox container = new VBox(10);
        container.getStyleClass().add("new-review-section");

        Label sectionTitle = new Label("Add Your Review");
        sectionTitle.getStyleClass().add("section-title");

        newReviewText.setPromptText("Write your review here...");
        newReviewText.setPrefRowCount(3);

        starRatingPicker.getItems().addAll(1, 2, 3, 4, 5);
        starRatingPicker.setPromptText("Select rating");

        Button submitButton = new Button("Submit Review");
        submitButton.setOnAction(e -> submitReview());

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(10);
        controlsGrid.setVgap(10);
        controlsGrid.add(new Label("Member:"), 0, 0);
        controlsGrid.add(memberPicker, 1, 0);
        controlsGrid.add(new Label("Rating:"), 0, 1);
        controlsGrid.add(starRatingPicker, 1, 1);
        controlsGrid.add(submitButton, 1, 2);

        container.getChildren().addAll(sectionTitle, newReviewText, controlsGrid);
        return container;
    }

    private void loadReviews() {
        reviews.clear();
        reviews.addAll(BookReview.getReviewsForBook(book));
        reviewsContainer.getChildren().clear();

        if (reviews.isEmpty()) {
            Label noReviewsLabel = new Label("No reviews yet");
            noReviewsLabel.getStyleClass().add("no-reviews-label");
            reviewsContainer.getChildren().add(noReviewsLabel);
            return;
        }

        for (BookReview review : reviews) {
            VBox reviewBox = createReviewBox(review);
            reviewsContainer.getChildren().add(reviewBox);
        }
    }

    private void loadMembers() {
        ObservableList<Member> members = FXCollections.observableArrayList();
        members.addAll(Member.getMembers());
        memberPicker.setItems(members);
        reservationMemberPicker.setItems(members);
    }

    private VBox createReviewBox(BookReview review) {
        VBox container = new VBox(5);
        container.getStyleClass().add("review-box");
        container.setPadding(new Insets(10));

        HBox header = new HBox(10);
        Label memberLabel = new Label(review.getMemberIdProperty().getValue().toString());
        memberLabel.getStyleClass().add("member-name-label");
        Label starsLabel = new Label(getStarDisplay(review.getRatingProperty().get()));
        Label dateLabel = new Label(formatDateTime(Date.from(Instant.now())));
        header.getChildren().addAll(memberLabel, starsLabel, dateLabel);

        Label reviewText = new Label(review.getTextProperty().get());
        reviewText.setWrapText(true);

        container.getChildren().addAll(header, reviewText);
        return container;
    }

    private VBox createReservationHistoryContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        // Title
        Label historyLabel = new Label("Reservation History");
        historyLabel.getStyleClass().add("section-title");

        // Create scrollable container for history entries
        VBox historyContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(historyContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(300);

        // Load and display reservation history
        var records = ReservationRecord.getByBook(book.getIdProperty().get());

        if (records.isEmpty()) {
            Label noHistoryLabel = new Label("No reservation history");
            noHistoryLabel.getStyleClass().add("no-reviews-label");
            historyContainer.getChildren().add(noHistoryLabel);
        } else {
            for (ReservationRecord record : records) {
                VBox recordBox = new VBox(5);
                recordBox.getStyleClass().add("review-box");
                recordBox.setPadding(new Insets(10));

                // Member name and reservation date
                HBox header = new HBox(10);
                Label memberLabel = new Label(record.getMember().getFullNameProperty().get());
                memberLabel.getStyleClass().add("member-name-label");

                // Format and display dates
                String reservedDate = formatDateTime(Date.from(record.getReservedAtProperty().get().toInstant()));
                Label reservedLabel = new Label("Reserved: " + reservedDate);

                header.getChildren().addAll(memberLabel);

                // Create status box
                HBox statusBox = new HBox(10);
                if (record.isReturned()) {
                    String returnedDate = formatDateTime(Date.from(record.getReturnedAtProperty().get().toInstant()));
                    Label returnedLabel = new Label("Returned: " + returnedDate);
                    statusBox.getChildren().add(returnedLabel);
                } else {
                    Label activeLabel = new Label("Currently Reserved");
                    activeLabel.setStyle("-fx-text-fill: #2196F3;"); // Blue color for active reservations
                    statusBox.getChildren().add(activeLabel);
                }

                recordBox.getChildren().addAll(header, reservedLabel, statusBox);
                historyContainer.getChildren().add(recordBox);
            }
        }

        container.getChildren().addAll(historyLabel, scrollPane);
        return container;
    }

    private void submitReview() {
        var rating = starRatingPicker.getValue();
        var text = newReviewText.getText().trim();
        var member = memberPicker.getValue();

        if (rating == null || text.isEmpty() || member == null) {
            showAlert("Please provide a member, rating, and review text.");
            return;
        }

        BookReview.create(member.getIdProperty().get(), book.getIdProperty().get(), text, rating);
        loadReviews();

        newReviewText.clear();
        starRatingPicker.setValue(null);
        memberPicker.setValue(null);

        loadReviews();
    }

    private String getStarDisplay(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? STAR : EMPTY_STAR);
        }
        return stars.toString();
    }

    private double calculateAverageRating() {
        if (reviews.isEmpty()) {
            loadReviews();

            if (reviews.isEmpty()) {
                return 0.0;
            }
        }
        return reviews.stream().mapToInt(r -> r.getRatingProperty().get()).average().orElse(0.0);
    }

    private String formatDateTime(Date dateTime) {
        return dateTime.toString();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}