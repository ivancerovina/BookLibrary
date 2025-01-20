package com.booklibrary.booklibrary.datatypes;

import com.booklibrary.booklibrary.database.Database;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.booklibrary.booklibrary.utils.Utils.showError;

public class Author {
    private final IntegerProperty idProperty;
    private final StringProperty fullNameProperty;

    private Author(int id, String fullName) {
        this.idProperty = new SimpleIntegerProperty(id);
        this.fullNameProperty = new SimpleStringProperty(fullName);
    }

    public static Author getById(int id) {
        var connection = Database.getInstance().getConnection();
        var query = "SELECT * FROM authors WHERE id = ?";

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);

            var rs = stmt.executeQuery();
            if (rs.next()) {
                return new Author(rs.getInt("id"), rs.getString("full_name"));
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching author by id [Author class]");
            e.printStackTrace();
        }

        return null;
    }

    public static void create(String fullName) {
        var connection = Database.getInstance().getConnection();
        var query = "INSERT INTO authors (full_name) VALUES (?)";

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setString(1, fullName);

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while creating author [Author class]");
            e.printStackTrace();
        }
    }

    public static List<Author> getAuthors() {
        var connection = Database.getInstance().getConnection();
        var query = "SELECT * FROM authors";
        var authors = new ArrayList<Author>();

        try {
            var stmt = connection.prepareStatement(query);
            var rs = stmt.executeQuery();

            while (rs.next()) {
                authors.add(new Author(rs.getInt("id"), rs.getString("full_name")));
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching authors [Author class]");
            e.printStackTrace();
        }

        return authors;
    }

    public IntegerProperty getIdProperty() {
        return idProperty;
    }

    public StringProperty getFullNameProperty() {
        return fullNameProperty;
    }

    public void delete() {
        var connection = Database.getInstance().getConnection();

        // Delete books
        try {
            var query = "DELETE FROM books WHERE author_id = ?";
            var stmt = connection.prepareStatement(query);
            stmt.setInt(1, idProperty.get());

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while deleting books by author [Author class]");
            e.printStackTrace();
        }

        // Delete author
        try {
            var query = "DELETE FROM authors WHERE id = ?";
            var stmt = connection.prepareStatement(query);
            stmt.setInt(1, idProperty.get());

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while deleting author [Author class]");
            e.printStackTrace();
        }
    }

    public List<String> getGenres() {
        var connection = Database.getInstance().getConnection();
        var query = "SELECT DISTINCT genre FROM books WHERE author_id = ?";
        var genres = new ArrayList<String>();

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setInt(1, idProperty.get());

            var rs = stmt.executeQuery();
            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching genres by author [Author class]");
            e.printStackTrace();
        }

        return genres;
    }

    public int getAverageRating() {
        var connection = Database.getInstance().getConnection();
        var query = "SELECT AVG(rating) FROM reviews WHERE book_id IN (SELECT id FROM books WHERE author_id = ?)";

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setInt(1, idProperty.get());

            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("An error occurred while fetching average rating by author [Author class]");
        }

        return 0;
    }

    @Override
    public String toString() {
        return fullNameProperty.get();
    }
}
