package org.chatapp;

import java.time.LocalDate;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.bson.types.ObjectId;
import org.chatapp.com.mongodb.Database;
import org.chatapp.network.chatClient;
import org.chatapp.ChatUser;

public class SceneController {
    private Stage stage;
    private Parent root;

    private chatClient client;

    static ChatUser curUser = null;

//    public void setChatClient(chatClient client) {
//        this.client = client;
//    }
    @FXML
    private Label errorMessage;

    @FXML
    private Label errorMessageSignUp;

    @FXML
    private TextField txtusername;

    @FXML
    private TextField txtpassword;

    @FXML
    private TextField txtfullname;

    @FXML
    private DatePicker dateOfBirth;

    static Database database = Database.getInstance();
    public Boolean Login() throws IOException{
        // Provide username and password
        String username = txtusername.getText();
        String password = txtpassword.getText();
        String name = "";
        ObjectId id = new ObjectId();
        boolean exists = database.userExists(username);
        if (exists) {
            System.out.println("User exists.");
            boolean validUser = database.verifyPassword(username, password);
            if(validUser){
                name = database.getName(username, password);
                id = database.getUserObjectId(username);
                curUser = new ChatUser(id, name, username);
                System.out.println("Authenticated!");
                return true;
            }else{
                errorMessage.setText("Incorrect Password");
                System.out.println("Not Authenticated");
                return false;
            }

        } else {
            errorMessage.setText("Not valid credentials");
            System.out.println("Username does not exist.");
            return false;
        }

    }

    public void loginPageToSignUpPage(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SignUpPage.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public Boolean signUp() throws IOException {
        String fullname = txtfullname.getText();
        String username = txtusername.getText();
        String dOB = dateOfBirth.getValue().toString();
        System.out.println(dOB);
        String password = txtpassword.getText();
        if(fullname.length() > 2 && username.length() > 4 && password.length() > 3 && !dOB.isEmpty()) {
            return database.createUser(fullname, username, password, dOB);
        }
        else{
            System.out.println("Invalid Length");
            errorMessageSignUp.setText("Fullname must be longer than 2. Username and password longer than 3.");
            return false;
        }
    }

    public void backToLogIn(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LogInPage.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void backToLogInAccountCreated(ActionEvent event) throws IOException {
        if (signUp()) {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LogInPage.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }

    public void loginPageToChatPage(ActionEvent event) throws IOException {
        if (Login()) {
            ChatPageController chatPageController = new ChatPageController();
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ChatPage.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }
}