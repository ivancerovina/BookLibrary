package com.booklibrary.booklibrary.datatypes;

import com.booklibrary.booklibrary.database.Database;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.booklibrary.booklibrary.utils.Utils.showError;

public class Book {
    private final IntegerProperty idProperty;
    private final StringProperty titleProperty;
    private final IntegerProperty authorIdProperty;
    private final StringProperty genreProperty;
    private final IntegerProperty yearProperty;
    private final IntegerProperty reservedByProperty;

    private Book(int id, String title, int authorId, String genre, int year, Integer reservedBy) {
        this.idProperty = new SimpleIntegerProperty(id);
        this.titleProperty = new SimpleStringProperty(title);
        this.authorIdProperty = new SimpleIntegerProperty(authorId);
        this.genreProperty = new SimpleStringProperty(genre);
        this.yearProperty = new SimpleIntegerProperty(year);
        this.reservedByProperty = reservedBy != null ? new SimpleIntegerProperty(reservedBy) : null;
    }

    public static void create(String title, int authorId, String genre, int year) {
        var conn = Database.getInstance().getConnection();
        var query = "INSERT INTO books (title, author_id, genre, year) VALUES (?, ?, ?, ?)";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setInt(2, authorId);
            stmt.setString(3, genre);
            stmt.setInt(4, year);

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while creating a book");
            e.printStackTrace();
        }
    }

    public static List<Book> getByAuthor(int authorId) {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT * FROM books WHERE author_id = ?";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, authorId);

            var rs = stmt.executeQuery();
            var books = new ArrayList<Book>();
            while (rs.next()) {
                books.add(new Book(rs.getInt("id"), rs.getString("title"), rs.getInt("author_id"), rs.getString("genre"), rs.getInt("year"), rs.findColumn("reserved_by") != 0 ? rs.getInt("reserved_by") : null));
            }

            return books;
        } catch (SQLException e) {
            showError("An error occurred while fetching books by author");
            e.printStackTrace();
        }

        return null;
    }

    public static List<Book> getBooks() {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT * FROM books";
        var books = new ArrayList<Book>();

        try {
            var stmt = conn.prepareStatement(query);
            var rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(new Book(rs.getInt("id"), rs.getString("title"), rs.getInt("author_id"), rs.getString("genre"), rs.getInt("year"), rs.findColumn("reserved_by") != 0 ? rs.getInt("reserved_by") : null));
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching books");
            e.printStackTrace();
        }

        return books;
    }

    public static Book findById(int i) {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT * FROM books WHERE id = ?";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, i);

            var rs = stmt.executeQuery();
            if (rs.next()) {
                return new Book(rs.getInt("id"), rs.getString("title"), rs.getInt("author_id"), rs.getString("genre"), rs.getInt("year"), rs.findColumn("reserved_by") != 0 ? rs.getInt("reserved_by") : null);
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching a book by id");
            e.printStackTrace();
        }

        return null;
    }

    public IntegerProperty getIdProperty() {
        return idProperty;
    }

    public StringProperty getTitleProperty() {
        return titleProperty;
    }

    public IntegerProperty getAuthorIdProperty() {
        return authorIdProperty;
    }

    public StringProperty getGenreProperty() {
        return genreProperty;
    }

    public IntegerProperty getYearProperty() {
        return yearProperty;
    }

    public IntegerProperty getReservedByProperty() {
        return reservedByProperty;
    }

    public Author getAuthor() {
        return Author.getById(authorIdProperty.get());
    }

    public int getAverageRating() {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT AVG(rating) FROM reviews WHERE book_id = ?";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, idProperty.get());

            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching average rating");
            e.printStackTrace();
        }

        return 0;
    }

    public boolean isReserved() {
        return reservedByProperty != null;
    }

    public Member getReservedByMember() {
        if (!this.isReserved()) {
            return null;
        }

        return Member.findById(reservedByProperty.get());
    }

    public void delete() {
        var conn = Database.getInstance().getConnection();
        var query = "DELETE FROM books WHERE id = ?";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, idProperty.get());

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while deleting a book");
            e.printStackTrace();
        }
    }

    public void reserve(Integer memberId) {
        var conn = Database.getInstance().getConnection();

        try {
            if (memberId != null) {
                var sql = "UPDATE books SET reserved_by = ? WHERE id = ?";
                var stmt = conn.prepareStatement(sql);

                stmt.setInt(1, memberId);
                stmt.setInt(2, idProperty.get());
                stmt.executeUpdate();
            } else {
                var sql = "UPDATE books SET reserved_by = NULL WHERE id = ?";
                var stmt = conn.prepareStatement(sql);

                stmt.setInt(1, idProperty.get());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            showError("An error occurred while reserving a book");
            e.printStackTrace();
        }
    }
}
