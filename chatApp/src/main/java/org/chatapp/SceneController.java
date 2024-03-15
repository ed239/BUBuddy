package org.chatapp;

import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.chatapp.com.mongodb.Database;
import org.chatapp.network.chatClient;

public class SceneController {
    private Stage stage;
    private Parent root;

    private chatClient client;

//    public void setChatClient(chatClient client) {
//        this.client = client;
//    }
    @FXML
    private Label lblcurrentuser;

    @FXML
    private TextField txtusername;

    @FXML
    private TextField txtpassword;

    @FXML
    private TextField txtfullname;
    Database database = Database.getInstance();
    public Boolean Login() throws IOException{
        // Provide username and password
        String username = txtusername.getText();
        String password = txtpassword.getText();

        boolean exists = database.userExists(username);
        if (exists) {
            System.out.println("User exists.");
            boolean validUser = database.verifyPassword(username, password);
            if(validUser){
                System.out.println("Authenticated!");
                return true;
            }else{
                System.out.println("Not Authenticated");
                return false;
            }

        } else {
            System.out.println("User does not exist.");
            return false;
        }

    }

    public void loginPageToSignUpPage(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SignUp2.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();

    }

    public void signUp(ActionEvent event) throws IOException {
        String fullname = txtfullname.getText();
        String username = txtusername.getText();
        String password = txtpassword.getText();

        database.createUser(fullname, username, password);
    }

    public void backToLogIn(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LogInPage.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

        public void loginPageToChatPage(ActionEvent event) throws IOException {
        if (Login()) {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ChatPage.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }
}