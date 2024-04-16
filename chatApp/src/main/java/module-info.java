module org.example.chatapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires ch.qos.logback.classic;
    requires org.slf4j;


    opens org.chatapp to javafx.fxml;
    exports org.chatapp;
}