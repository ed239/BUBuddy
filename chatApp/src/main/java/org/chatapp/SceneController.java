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
import java.util.regex.Pattern;

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
    private TextField newPasswordField;
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
    private String passwordErrorMsg = "Password needs to have at least 8 characters, 1 capital "
        + "letter, 1 number and 1 additional symbol.";

    static Database database = Database.getInstance();

    public Boolean Login() throws IOException{
        // Provide username and password
//        curUser = null;
        String username = txtusername.getText().toLowerCase();
        String password = txtpassword.getText();
        String name = "";
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
        String username =  txtusername.getText().toLowerCase();
        String userDob = dateOfBirth.getValue().toString();
        String newPassword = newPasswordField.getText();
        String verifyNewPassword = verifyNewPasswordField.getText();

        if(username.isEmpty()){
            errorMessagePassword.setText("Please provide Username and Date of Birth");
            return false;
        } else if (userDob.isEmpty()) {
            errorMessagePassword.setText("Please provide Date of Birth");
            return false;
        }
        boolean validCred = database.validEmailDob(username,userDob);
        if(!validCred){
                errorMessagePassword.setText("Incorrect Credentials");
                return false;
            }
        //CHANGNG THE PASSWORD
        if(newPassword.isEmpty()){
            errorResetPassword.setText("Please provide new password!");
            System.out.println("\nPLEASE PROVIDE NEW PASSWORD!\n");
            return false;
        }
        if (!validatePassword(newPassword)) {
            errorResetPassword.setText(passwordErrorMsg);
            System.out.println(passwordErrorMsg);
            return false;
        }
        if(!(newPassword.equals(verifyNewPassword))){
            errorResetPassword.setText("Passwords do not match!");
            System.out.println("\nPASSWORD DO NOT MATCH\n");
            return false;
        }

        boolean passwordUpdated = database.updatePassword(username, newPassword);
        if(passwordUpdated){
            return true;
        }else {
//            errorResetPassword.setText("Failed to update password!");
            System.out.println("RESET FAILED");
            return false;
        }


    }


    // RESET NEW PASSWORD AND UPDATE
//    public boolean resetPassword() throws IOException{
//        String username = getUserForPass();
//        System.out.println("EDEN USe");
//        System.out.println(username);
//        String newPassword = newPasswordField.getText();
//        String verifyNewPassword = verifyNewPasswordField.getText();
//        if(newPassword.isEmpty()){
//            errorResetPassword.setText("Please provide new password!");
//            System.out.println("\nPLEASE PROVIDE NEW PASSWORD!\n");
//            return false;
//        }
//        if (!validatePassword(newPassword)) {
//            errorResetPassword.setText(passwordErrorMsg);
//            System.out.println(passwordErrorMsg);
//            return false;
//        }
//        if(!(newPassword.equals(verifyNewPassword))){
//            errorResetPassword.setText("Passwords do not match!");
//            System.out.println("\nPASSWORD DO NOT MATCH\n");
//            return false;
//        }
//
//        boolean passwordUpdated = database.updatePassword("ed@bu.edu", newPassword);
//        if(passwordUpdated){
//            return true;
//        }else {
////            errorResetPassword.setText("Failed to update password!");
//            System.out.println("RESET FAILED");
//            return false;
//        }
//    }
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
        if(checkUserName()){
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SuccessMessages.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }else{
            errorMessage.setText("Unable to change password: check inputs");
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
        String username = txtusername.getText().toLowerCase();
        String dOB = dateOfBirth.getValue().toString();
        String password = txtpassword.getText();
        byte[] profileImageDate = null; // SET TO NULL INITIALLY, USER CAN PROVIDE IT LATER
        String email = "";

        // User Req:
        // full name needs to be at least 2 characters.
        // username needs to be longer than 7 characters and contain "@bu.edu".
        // dOB needs to have a selection.
        // password needs to have at least 8 characters, 1 capital letter, 1 number and 1 symbol

        if(fullname.length() > 2 && username.length() > 7 && username.contains("@bu.edu") &&
            !dOB.isEmpty()) {
            boolean validPass = validatePassword(password);
            if (validPass) {
                return database.createUser(fullname, username, password, dOB, profileImageDate,
                    email);
            } else {
                errorMessageSignUp.setText(passwordErrorMsg);
                return false;
            }
        } else {
            System.out.println("Invalid Length");
            errorMessageSignUp.setText(
                "Fullname must be longer than 2. Username needs to contain \"@bu.edu\" and password longer than 3.");
            return false;
        }
    }

    public Boolean validatePassword(String password) {
        Pattern symbol = Pattern.compile("[^a-zA-Z0-9 ]");
        Pattern upperCase = Pattern.compile("[A-Z ]");
        Pattern lowerCase = Pattern.compile("[a-z ]");
        Pattern number = Pattern.compile("[0-9 ]");
        boolean isPassValid = true;

        if (password.length() < 8) {
            isPassValid=false;
        }
        if (!symbol.matcher(password).find()) {
            isPassValid=false;
        }
        if (!upperCase.matcher(password).find()) {
            isPassValid=false;
        }
        if (!lowerCase.matcher(password).find()) {
            isPassValid=false;
        }
        if (!number.matcher(password).find()) {
            isPassValid=false;
        }
        return isPassValid;
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

//    public void forgotPasswordToResetPassword(ActionEvent event) throws IOException{
//        if (checkUserName()){
//            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ResetPassword.fxml")));
//            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
//            stage.getScene().setRoot(root);
//            stage.show();
//        }
//    }

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