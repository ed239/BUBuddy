module org.example.chatapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.chatapp to javafx.fxml;
    exports org.example.chatapp;
}