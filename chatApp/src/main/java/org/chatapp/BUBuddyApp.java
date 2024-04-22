package org.chatapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
//----------------------------------------------------------------------------------------------------------------------
//
//  BUBuddyApp
//      Starts the chat Application: run server before running chat application to use local chat
//
//  Authors: Eden Dubrovsky, Jacob Kustra, Bolatbek Amiyev, Azamat Omirzak
//  Date: Spring 2024
//  Class: MET CS622
//  Issues:
//        1. Problem with resizing window by mouse
//        2. After changing the name in the Profile, the application must be re-launched so that others can see the new name
//        3. If you click the logout button, the client socket will not close
//        4. If the client couldn't connect to the server socket, the application will start because the main chat works with MongoDB,
//        but if the client wants to connect to the server, he needs to restart the application
//        5. If the user goes to Profile page and back to Chat page, in the local chat, other users must send the message twice to be seen,
//        and only every second message can be viewed. The number of times the user will go to Profile and back to Chat page,
//        the number of times they(other users) will need to send messages to be seen. This problem is not with the socket, but with JavaFX and Thread.
//        6. Separating ChatContainer for different chats
//   All this is doable, it just takes time
//
//

public class BUBuddyApp extends Application {
    private final int sceneWidth = 1000;
    private final int sceneHeight = 700;
    
    ///////////////////////////////////////////////////////////////////
    /// start() Starts the application on the login page            ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BUBuddyApp.class.getResource("LoginPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), sceneWidth, sceneHeight);

        stage.setScene(scene);
        stage.setMinHeight(533);
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