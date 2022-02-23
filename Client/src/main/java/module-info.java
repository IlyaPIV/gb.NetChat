module Client {
    requires javafx.controls;
    requires javafx.fxml;
    requires MyAPI;


    opens client to javafx.fxml;
    exports client;

}