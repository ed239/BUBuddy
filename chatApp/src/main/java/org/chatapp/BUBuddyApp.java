package org.chatapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import org.chatapp.network.chatClient;
import org.chatapp.network.chatServer;


public class BUBuddyApp extends Application {
    private final int sceneWidth = 1100;
    private final int sceneHeight = 700;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BUBuddyApp.class.getResource("LogInPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), sceneWidth, sceneHeight);
//        Scene scene = new Scene(fxmlLoader.load(), 640, 450);

        //creates new client each time your run
        SceneController sceneController = fxmlLoader.getController();
//        sceneController.setChatClient(newChatClient);

        stage.setScene(scene);
        stage.setMinHeight(600);
        stage.setMinWidth(550);
        stage.show();

        // Closing out of Window closes application.
        Platform.setImplicitExit(true);
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }
}