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
//  Issues: None known
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