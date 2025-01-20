module com.booklibrary.booklibrary {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.booklibrary.booklibrary to javafx.fxml;
    exports com.booklibrary.booklibrary;
    exports com.booklibrary.booklibrary.controllers;
    opens com.booklibrary.booklibrary.controllers to javafx.fxml;
}