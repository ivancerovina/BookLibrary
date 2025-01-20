package com.booklibrary.booklibrary.datatypes;

import com.booklibrary.booklibrary.database.Database;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookReview {
    private final IntegerProperty idProperty;
    private final IntegerProperty memberIdProperty;
    private final StringProperty textProperty;
    private final IntegerProperty ratingProperty;

    private BookReview(int id, int memberId, String text, int stars) {
        this.idProperty = new SimpleIntegerProperty(id);
        this.memberIdProperty = new SimpleIntegerProperty(memberId);
        this.textProperty = new SimpleStringProperty(text);
        this.ratingProperty = new SimpleIntegerProperty(stars);
    }

    public IntegerProperty getIdProperty() {
        return idProperty;
    }

    public IntegerProperty getMemberIdProperty() {
        return memberIdProperty;
    }

    public StringProperty getTextProperty() {
        return textProperty;
    }

    public IntegerProperty getRatingProperty() {
        return ratingProperty;
    }

    public static List<BookReview> getReviewsForBook(Book book) {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT * FROM reviews WHERE book_id = ?";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, book.getIdProperty().get());

            var result = stmt.executeQuery();
            var reviews = new ArrayList<BookReview>();
            while (result.next()) {
                var review = new BookReview(
                    result.getInt("id"),
                    result.getInt("member_id"),
                    result.getString("text"),
                    result.getInt("rating")
                );
                reviews.add(review);
            }
            return reviews;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<BookReview> getByMember(int memberId) {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT * FROM reviews WHERE member_id = ?";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, memberId);

            var result = stmt.executeQuery();
            var reviews = new ArrayList<BookReview>();
            while (result.next()) {
                var review = new BookReview(
                    result.getInt("id"),
                    result.getInt("member_id"),
                    result.getString("text"),
                    result.getInt("rating")
                );
                reviews.add(review);
            }
            return reviews;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static void create(int memberId, int bookId, String text, int rating) {
        var conn = Database.getInstance().getConnection();
        var query = "INSERT INTO reviews (member_id, book_id, text, rating) VALUES (?, ?, ?, ?)";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, memberId);
            stmt.setInt(2, bookId);
            stmt.setString(3, text);
            stmt.setInt(4, rating);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
