module org.example.chatapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.chatapp to javafx.fxml;
    exports org.chatapp;
}