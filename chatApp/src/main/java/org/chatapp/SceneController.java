package org.chatapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.chatapp.com.mongodb.Database;
import java.util.regex.Pattern;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class SceneController {
    private Stage stage;
    private Parent root;

    static ChatUser curUser = null;

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
    @FXML
    private ImageView id_eye_hide;
    @FXML
    private ImageView id_eye_show;
    @FXML
    private TextField txt_field_password;
    private String passwordErrorMsg = "Password needs to have at least 8 characters, 1 capital "
        + "letter, 1 number and 1 additional symbol.";

    static Database database = Database.getInstance();

    ///////////////////////////////////////////////////////////////////
    /// Login() button to log in to application                     ///
    /// Input : takes from the user input on screen...              ///
    ///  username, password                                         ///
    /// Output: Boolean - valid credentials - true, else false      ///
    /// Verifies login and routes user to chat page                 ///
    public Boolean Login() throws IOException{
        String username = txtusername.getText().toLowerCase();
        String password = txtpassword.getText();
        boolean exists = database.userExists(username);
        if (exists) {
            System.out.println("User exists.");
            boolean validUser = database.verifyPassword(username, password);
            if(validUser){
                String name = database.getName(username);
                curUser = database.getChatUser(name);
                return true;
            }else{
                errorMessage.setText("Incorrect Password");
                return false;
            }
        } else {
            errorMessage.setText("Not valid credentials");
            return false;
        }
    }

    // CHECK THE USERNAME AND DATE OF BIRTH WHETHER THEY ARE MATCH OR NOT:
    public Boolean checkUserName() throws IOException{
        String username =  txtusername.getText().toLowerCase();
//        String userdob = dateOfBirth.getValue().toString();
        LocalDate userDob = dateOfBirth.getValue();
        String dob = "";
        if(username.isEmpty()){
            errorMessagePassword.setText("Please provide Username and Date of Birth");
            System.out.println("\n---->>>> checkUserName()>>> FROM SCENE CONTROLLER:");
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
            System.out.println("\nUSER EXISTS\n");
            boolean validUserName = database.verifyDateOfBirth(username, userDobStr);
//            System.out.println("Check validUsername");
            if(validUserName){
                String name = database.getName(username);  // GET CURRENT USERNAME FROM DATABASE
                curUser = database.getChatUser(name);   // CURRENT CHAT USER: -> {USERNAME = 'ggg12'}
                System.out.println("\nFROM SCENE CONTROLLER: checkUserName() Method+++++++++");
                System.out.println(curUser);
                System.out.print("-------->>> curUser >>>> RESET PASSWORD PAGE IS OPEN: -> ");
                System.out.println("-------->>> RESET YOUR PASSWORD!\n");
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
    ///////////////////////////////////////////////////////////////////
    /// checkandupdatePass() log in to application                  ///
    /// Input : takes from the user input on screen...              ///
    ///  username, dob, newpassword, verified password              ///
    /// Output: Boolean - true if password reset else false         ///
    /// calls database to reset the password                        ///
    public Boolean checkandupdatePass() throws IOException{
        String username =  txtusername.getText().toLowerCase();
        String userDob = dateOfBirth.getValue().toString();
        String newPassword = newPasswordField.getText();
        String verifyNewPassword = verifyNewPasswordField.getText();

        // Verifying username and dob are not empty
        if(username.isEmpty()){
            errorMessagePassword.setText("Please provide Username and Date of Birth");
            return false;
        } else if (userDob.isEmpty()) {
            errorMessagePassword.setText("Please provide Date of Birth");
            return false;
        }
        //veridying username and dob match a user in the db
        boolean validCred = database.validEmailDob(username,userDob);
        if(!validCred){
            errorMessagePassword.setText("Incorrect Credentials");
            return false;
        }
        //Checking the new password fields are not empty and match
        if(newPassword.isEmpty()){
            errorResetPassword.setText("Please provide new password!");
            return false;
        }
        if (!validatePassword(newPassword)) {
            errorResetPassword.setText(passwordErrorMsg);
            System.out.println(passwordErrorMsg);
            return false;
        }
        if(!(newPassword.equals(verifyNewPassword))){
            errorResetPassword.setText("Passwords do not match!");
            return false;
        }
        //CHANGING THE PASSWORD
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
    public boolean resetPassword() throws IOException{
        String newPassword = newPasswordField.getText();
        String verifyNewPassword = verifyNewPasswordField.getText();
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
        if(!newPassword.equals(verifyNewPassword)){
            errorResetPassword.setText("Passwords do not match!");
            System.out.println("\nPASSWORD DO NOT MATCH\n");
            return false;
        }
        // GET CURRENT LOGGED-IN USER:
        String username = (curUser != null) ? curUser.getUsername() : null;
        if (username == null) {
            System.out.println("\nUser not logged in or username not available!");
            // Handle this case appropriately, such as showing an error message to the user.
            return false; // Or perform other actions based on your application's logic.
        }
//        // GET CURRENT LOGGED-IN USER:
//        username = curUser.getUsername();

        //UPDATE THE PASSWORD IN THE DATABASE:
        System.out.println("\nAttempting to update password for user: " + username + "\n");
        boolean passwordUpdated = database.updatePassword(username, newPassword);
        System.out.println("\nPassword updated in database: " + passwordUpdated);
        if(passwordUpdated){
            System.out.println();
            System.out.println("--->>> FROM SCENE CONTROLLER:");
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
//    @FXML
//    void changeVisibility(ActionEvent event){
//        if(checkBox.isSelected()){
//            passwordText.setText(verifyNewPasswordField.getText());
//            passwordText.setVisible(true);
//            verifyNewPasswordField.setVisible(false);
//            return;
//        }
//        verifyNewPasswordField.setText(passwordText.getText());
//        verifyNewPasswordField.setVisible(true);
//        passwordText.setVisible(false);
//    }
//    txt_field_password       ;       txtpassword ;      id_eye_hide       ;      id_eye_show

    @FXML
    void changeVisibility(MouseEvent event){
        ImageView clickedImage = (ImageView) event.getSource();
        if(clickedImage.getId().equals("id_eye_hide")){
            // Show password
            txt_field_password.setText(txtpassword.getText());
            txt_field_password.setVisible(true);
            txtpassword.setVisible(false);
            id_eye_show.setVisible(true);
            id_eye_hide.setVisible(false);
        }else if (clickedImage.getId().equals("id_eye_show")){
            // Hide the password
            txtpassword.setText(txt_field_password.getText());
            txt_field_password.setVisible(false);
            txtpassword.setVisible(true);
            id_eye_show.setVisible(false);
            id_eye_hide.setVisible(true);
        }
    }

//    @FXML
//    void showUp(MouseEvent event) {
//        ImageView clickedImage = (ImageView) event.getSource();
//        if (clickedImage.getId().equals("eye_show")) {
//            // Show the password
//            passwordText.setText(newPasswordField.getText());
//            passwordText.setVisible(true);
//            verifyNewPasswordField.setVisible(false);
//            eye_hide.setVisible(true);
//            eye_show.setVisible(false);
//        } else if (clickedImage.getId().equals("eye_hide")) {
//            // Hide the password
//            passwordText.setVisible(false);
//            verifyNewPasswordField.setVisible(true);
//            eye_show.setVisible(true);
//            eye_hide.setVisible(false);
//        }
//    }

    public void resetPasswordToSuccesMessage(ActionEvent event) throws IOException{
        if(resetPassword()){
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SuccessMessages.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }
    ///////////////////////////////////////////////////////////////////
    /// resetPassNotLoggedIn() Reset Password Button                ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// Action: if password updated, screen goes to success page    ///
    public void resetPassNotLoggedIn(ActionEvent event) throws IOException{
        if(checkandupdatePass()){
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
                return database.createUser(fullname, username, password, dOB, profileImageDate);
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

    ///////////////////////////////////////////////////////////////////
    /// forgotPasswordLogIn() Go to Forgot Password                 ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// Action: used when user is not logged in... from Log in page ///
    public void forgotPasswordLogIn(ActionEvent event) throws IOException{
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ForgotTest2.fxml")));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

}