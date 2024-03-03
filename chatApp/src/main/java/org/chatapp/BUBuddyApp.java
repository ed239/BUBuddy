package org.chatapp;

import javafx.application.Application;
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
        FXMLLoader fxmlLoader = new FXMLLoader(BUBuddyApp.class.getResource("LoginPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), sceneWidth, sceneHeight);

        //creates new client each time your run
        SceneController sceneController = fxmlLoader.getController();
        chatClient newChatClient = new chatClient(sceneController);
        sceneController.setChatClient(newChatClient);



        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        //comment these two out if running them independently
        new Thread(chatServer::main).start();
        new Thread(chatClient::main).start();
        launch();
    }
}