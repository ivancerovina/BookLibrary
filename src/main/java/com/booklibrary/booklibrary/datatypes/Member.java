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

public class Member {
    private final IntegerProperty idProperty;
    private final StringProperty fullNameProperty;

    private Member(int id, String fullName) {
        this.idProperty = new SimpleIntegerProperty(id);
        this.fullNameProperty = new SimpleStringProperty(fullName);
    }

    public static void create(String fullName) {
        var connection = Database.getInstance().getConnection();
        var query = "INSERT INTO members (full_name) VALUES (?)";

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setString(1, fullName);

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while creating a member");
            e.printStackTrace();
        }
    }

    public static Member findById(int id) {
        var connection = Database.getInstance().getConnection();
        var query = "SELECT * FROM members WHERE id = ?";

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);

            var result = stmt.executeQuery();
            if (result.next()) {
                return new Member(result.getInt("id"), result.getString("full_name"));
            }
        } catch (SQLException e) {
            showError("An error occurred while trying to find a member by id");
            e.printStackTrace();
        }
        return null;
    }

    public static Member search(String search) {
        var connection = Database.getInstance().getConnection();
        var query = "SELECT * FROM members WHERE full_name LIKE ?";

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setString(1, search);

            var result = stmt.executeQuery();
            if (result.next()) {
                return new Member(result.getInt("id"), result.getString("full_name"));
            }
        } catch (SQLException e) {
            showError("An error occurred while searching for members");
            e.printStackTrace();
        }

        return null;
    }

    public static List<Member> getMembers() {
        var connection = Database.getInstance().getConnection();
        var query = "SELECT * FROM members";
        var members = new ArrayList<Member>();

        try {
            var stmt = connection.prepareStatement(query);
            var result = stmt.executeQuery();

            while (result.next()) {
                members.add(new Member(result.getInt("id"), result.getString("full_name")));
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching members");
            e.printStackTrace();
        }

        return members;
    }

    public IntegerProperty getIdProperty() {
        return idProperty;
    }

    public StringProperty getFullNameProperty() {
        return fullNameProperty;
    }

    public void createReview(Book book, String review, int stars) {
        if (stars > 5 || stars < 1) {
            showError("Stars must be between 1 and 5");
        }

        var connection = Database.getInstance().getConnection();
        var query = "INSERT INTO reviews (member_id, book_id, review, rating) VALUES (?, ?, ?, ?)";

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setInt(1, this.idProperty.get());
            stmt.setInt(2, book.getIdProperty().get());
            stmt.setString(3, review);
            stmt.setInt(4, stars);

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while creating a review for a book");
            e.printStackTrace();
        }
    }

    public List<BookReview> getReviews() {
        return BookReview.getByMember(this.idProperty.get());
    }

    public void delete() {
        var connection = Database.getInstance().getConnection();
        var query = "DELETE FROM members WHERE id = ?";

        try {
            var stmt = connection.prepareStatement(query);
            stmt.setInt(1, this.idProperty.get());

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while deleting a member");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return getFullNameProperty().get();
    }
}
