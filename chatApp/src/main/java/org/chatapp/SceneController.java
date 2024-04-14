package org.chatapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.chatapp.com.mongodb.Database;
import org.chatapp.network.chatClient;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

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

    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField verifyNewPasswordField;
    @FXML
    private TextField passwordText;
    @FXML
    private CheckBox checkBox;
    @FXML
    private Label errorMessagePassword;
    @FXML
    private Label errorResetPassword;

    static Database database = Database.getInstance();

    public Boolean Login() throws IOException{
        // Provide username and password
//        curUser = null;
        String username = txtusername.getText();
        String password = txtpassword.getText();
        String name = "";
//        String dob = "";
//        ObjectId id = new ObjectId();
        boolean exists = database.userExists(username);
        if (exists) {
            System.out.println("User exists.");
            boolean validUser = database.verifyPassword(username, password);
            if(validUser){
                name = database.getName(username);
//                id = database.getUserObjectId(username);
//                dob = database.getDOB(username);
                curUser = database.getChatUser(name);
                System.out.println("\n\nSCENECONTROLLER");
                System.out.println(curUser);
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

    // CHECK THE USERNAME AND DATE OF BIRTH WHETHER THEY ARE MATCH OR NOT:
    public Boolean checkUserName() throws IOException{
        String username =  txtusername.getText();
//        String userdob = dateOfBirth.getValue().toString();
        LocalDate userDob = dateOfBirth.getValue();
        String dob = "";
        if(username.isEmpty()){
            errorMessagePassword.setText("Please provide Username and Date of Birth");
            System.out.println("\nFROM SCENE CONTROLLER:");
            System.out.println("USERNAME: -> " + curUser);
            System.out.println("PLEASE PROVIDE USERNAME ANS DATE OF BIRTH!\n");
            return false;
        } else if (userDob == null) {
            errorMessagePassword.setText("Please provide Date of Birth");
            System.out.println("\nFROM SCENE CONTROLLER:");
            System.out.println("USERNAME: -> " + curUser);
            System.out.println("PLEASE PROVIDE DATE OF BIRTH!\n");
            return false;
        }
        String userDobStr = userDob.toString();
        boolean exists = database.userExists(username);
        if(exists){
            System.out.println("USER EXISTS");
            boolean validUserName = database.verifyDateOfBirth(username, userDobStr);
//            System.out.println("Check validUsername");
            if(validUserName){
//                dob = database.getDOB(username);    CURRENT CHAT USER: -> {USERNAME = 'null'}
                curUser = database.getChatUser(username);   // CURRENT CHAT USER: -> {USERNAME = 'ggg12'}
                System.out.println("\nFROM SCENE CONTROLLER:");
                System.out.println(curUser);
                System.out.print("RESET PASSWORD PAGE IS OPEN: -> ");
                System.out.println("RESET YOUR PASSWORD!\n");
                return true;
            }else {
                errorMessagePassword.setText("Incorrect Date of Brith");
                System.out.println("\nINCORRECT DATE OF BIRTH");
                System.out.println("PLEASE TRY AGAIN!\n");
                return false;
            }
        }else {
            errorMessagePassword.setText("Not valid credentials");
            System.out.println("\nUSER NOT EXISTS, PLEASE,TRY AGAIN!");
            return false;
        }
    }

    // RESET NEW PASSWORD AND UPDATE
    public boolean resetPassword() throws IOException{
        String newPassword = newPasswordField.getText();
        String verifyNewPassword = verifyNewPasswordField.getText();
        if(newPassword.isEmpty()){
            errorResetPassword.setText("Please provide new password!");
            System.out.println("\nPLEASE PROVIDE NEW PASSWORD!\n");
            return false;
        }
        if(!newPassword.equals(verifyNewPassword)){
            errorResetPassword.setText("Passwords do not match!");
            System.out.println("\nPASSWORD DO NOT MATCH\n");
            return false;
        }
        // GET CURRENT LOGGED-IN USER:
        String username = curUser.getUsername();
        //UPDATE THE PASSWORD IN THE DATABASE:
        System.out.println("\nAttempting to update password for user: " + username + "\n");
        boolean passwordUpdated = database.updatePassword(username, newPassword);
        System.out.println("\nPassword updated in database: " + passwordUpdated);
        if(passwordUpdated){
            System.out.println();
            System.out.println("FROM SCENE CONTROLLER:");
            System.out.println(curUser);
            System.out.println("PASSWORD UPDATED SUCCESSFULLY!");
            System.out.println();
            return true;
        }else {
            // FAILED TO UPDATE PASSWORD
            errorResetPassword.setText("Failed to update password!");
            System.out.println("\nFAILED TO UPDATE PASSWORD!\n");
            return false;
        }
    }
    // CHANGE THE VISIBILITY TO THE PASSWORD BY CLICKING SHOW-PASSWORD BUTTON
    @FXML
    void changeVisibility(ActionEvent event){
        if(checkBox.isSelected()){
            passwordText.setText(verifyNewPasswordField.getText());
            passwordText.setVisible(true);
            verifyNewPasswordField.setVisible(false);
            return;
        }
        verifyNewPasswordField.setText(passwordText.getText());
        verifyNewPasswordField.setVisible(true);
        passwordText.setVisible(false);
    }

    public void resetPasswordToSuccesMessage(ActionEvent event) throws IOException{
        if(resetPassword()){
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SuccessMessages.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }
    public void SuccessMessagesToLogInPage(ActionEvent event) throws IOException{
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LoginPage.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
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
        String password = txtpassword.getText();
        byte[] profileImageDate = null; // SET TO NULL INITIALLY, USER CAN PROVIDE IT LATER
        String email = "";
        if(fullname.length() > 2 && username.length() > 4 && password.length() > 3 && !dOB.isEmpty()) {
            return database.createUser(fullname, username, password, dOB, profileImageDate, email);
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

    public void forgotPasswordToResetPassword(ActionEvent event) throws IOException{
        if (checkUserName()){
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ResetPassword.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }

    // GO TO LOG-IN-PAGE, IF YOU ALREADY HAVE AN ACCOUNT
    public void backToSignInPage(ActionEvent event) throws IOException{
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LoginPage.fxml")));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    // GO TO SIGN-UP-PAGE  FROM  FORGOT-PASSWORD-PAGE,  IF YOU WANT TO CREATE ACCOUNT
    public void loginPageToCreateAccount(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SignUpPage.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    // GO TO --> FORGOT PASSWORD PAGE <--
    public void forgotPassword(ActionEvent event) throws IOException{
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ForgotTest.fxml")));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

}