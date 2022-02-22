//module chat.netchat {
module Client {
    requires javafx.controls;
    requires javafx.fxml;


    opens client to javafx.fxml;
    exports client;

}