module org.example.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;


    opens org.chatapp to javafx.fxml;
    exports org.chatapp;
}