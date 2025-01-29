package com.booklibrary.booklibrary.datatypes;

import com.booklibrary.booklibrary.database.Database;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.booklibrary.booklibrary.utils.Utils.showError;

public class ReservationRecord {
    private final IntegerProperty idProperty;
    private final IntegerProperty memberIdProperty;
    private final IntegerProperty bookIdProperty;
    private final ObjectProperty<Timestamp> reservedAtProperty;
    private final ObjectProperty<Timestamp> returnedAtProperty;

    private ReservationRecord(int id, int memberId, int bookId, Timestamp reservedAt, Timestamp returnedAt) {
        this.idProperty = new SimpleIntegerProperty(id);
        this.memberIdProperty = new SimpleIntegerProperty(memberId);
        this.bookIdProperty = new SimpleIntegerProperty(bookId);
        this.reservedAtProperty = new SimpleObjectProperty<>(reservedAt);
        this.returnedAtProperty = new SimpleObjectProperty<>(returnedAt);
    }

    public static void create(int memberId, int bookId, Timestamp reservedAt) {
        var conn = Database.getInstance().getConnection();
        var query = "INSERT INTO reservation_records (member_id, book_id, reserved_at) VALUES (?, ?, ?)";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, memberId);
            stmt.setInt(2, bookId);
            stmt.setTimestamp(3, reservedAt);

            stmt.executeUpdate();
        } catch (SQLException e) {
            showError("An error occurred while creating a reservation record");
            e.printStackTrace();
        }
    }

    public static List<ReservationRecord> getAll() {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT * FROM reservation_records ORDER BY reserved_at DESC";
        var records = new ArrayList<ReservationRecord>();

        try {
            var stmt = conn.prepareStatement(query);
            var rs = stmt.executeQuery();

            while (rs.next()) {
                records.add(new ReservationRecord(
                    rs.getInt("id"),
                    rs.getInt("member_id"),
                    rs.getInt("book_id"),
                    rs.getTimestamp("reserved_at"),
                    rs.getTimestamp("returned_at")
                ));
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching reservation records");
            e.printStackTrace();
        }

        return records;
    }

    public static List<ReservationRecord> getByBook(int bookId) {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT * FROM reservation_records WHERE book_id = ? ORDER BY reserved_at DESC";
        var records = new ArrayList<ReservationRecord>();

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, bookId);

            var rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(new ReservationRecord(
                    rs.getInt("id"),
                    rs.getInt("member_id"),
                    rs.getInt("book_id"),
                    rs.getTimestamp("reserved_at"),
                    rs.getTimestamp("returned_at")
                ));
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching reservation records by book");
            e.printStackTrace();
        }

        return records;
    }

    public static List<ReservationRecord> getByMember(int memberId) {
        var conn = Database.getInstance().getConnection();
        var query = "SELECT * FROM reservation_records WHERE member_id = ? ORDER BY reserved_at DESC";
        var records = new ArrayList<ReservationRecord>();

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, memberId);

            var rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(new ReservationRecord(
                    rs.getInt("id"),
                    rs.getInt("member_id"),
                    rs.getInt("book_id"),
                    rs.getTimestamp("reserved_at"),
                    rs.getTimestamp("returned_at")
                ));
            }
        } catch (SQLException e) {
            showError("An error occurred while fetching reservation records by member");
            e.printStackTrace();
        }

        return records;
    }

    public void markAsReturned() {
        var conn = Database.getInstance().getConnection();
        var query = "UPDATE reservation_records SET returned_at = CURRENT_TIMESTAMP WHERE id = ?";

        try {
            var stmt = conn.prepareStatement(query);
            stmt.setInt(1, idProperty.get());

            stmt.executeUpdate();
            returnedAtProperty.set(new Timestamp(System.currentTimeMillis()));
        } catch (SQLException e) {
            showError("An error occurred while marking reservation as returned");
            e.printStackTrace();
        }
    }

    // Getters for JavaFX properties
    public IntegerProperty getIdProperty() {
        return idProperty;
    }

    public IntegerProperty getMemberIdProperty() {
        return memberIdProperty;
    }

    public IntegerProperty getBookIdProperty() {
        return bookIdProperty;
    }

    public ObjectProperty<Timestamp> getReservedAtProperty() {
        return reservedAtProperty;
    }

    public ObjectProperty<Timestamp> getReturnedAtProperty() {
        return returnedAtProperty;
    }

    // Convenience methods to get related objects
    public Member getMember() {
        return Member.findById(memberIdProperty.get());
    }

    public Book getBook() {
        return Book.findById(bookIdProperty.get());
    }

    public boolean isReturned() {
        return returnedAtProperty.get() != null;
    }
}