package org.chatapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.chatapp.com.mongodb.Database;
import java.util.regex.Pattern;
import java.io.IOException;
import java.util.Objects;

//
// Class: SceneController
//
// Description:
//     This is a Scene Controller between Login, Forgot Password, Sign Up, and the Main Chat Page with related functionality
//     Actions provided: Create new account, log in, reset password
//
public class SceneController {
    private Stage stage;
    private Parent root;
    static ChatUser curUser = null;
    static String ip = "";
    @FXML
    private Label errorMessageForgotPass;
    @FXML
    private TextField serverIP;
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
    private Label errorMessagePassword;
    @FXML
    private Label errorResetPassword;
    @FXML
    private ImageView id_eye_hide;
    @FXML
    private ImageView id_eye_show;
    @FXML
    private ImageView id_eye_hide_1;
    @FXML
    private ImageView id_eye_show_1;
    @FXML
    private TextField txt_field_password;
    @FXML
    private TextField txt_field_password_1;
    private String passwordErrorMsg = "Password needs to have at least 8 characters, 1 capital "
        + "letter, 1 number and 1 additional symbol.";
    static Database database = Database.getInstance();

    ///////////////////////////////////////////////////////////////////
    /// Login() button to log in to application                     ///
    /// Input : takes from the user input on screen...              ///
    ///  username, password                                         ///
    /// Output: Boolean - valid credentials - true, else false      ///
    /// Verifies login and routes user to chat page                 ///
    ///////////////////////////////////////////////////////////////////
    public Boolean Login() throws IOException{
        String username = txtusername.getText().toLowerCase();
        String password = txtpassword.getText();
        boolean exists = database.userExists(username);
        if (exists) {
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

    ///////////////////////////////////////////////////////////////////
    /// checkandupdatePass() log in to application                  ///
    /// Input : takes from the user input on screen...              ///
    ///  username, dob, newpassword, verified password              ///
    /// Output: Boolean - true if password reset else false         ///
    /// calls database to reset the password                        ///
    ///////////////////////////////////////////////////////////////////
    public Boolean checkandupdatePass() throws IOException{
        String username =  txtusername.getText().toLowerCase();
        String userDob ="";
        String newPassword = newPasswordField.getText();
        String verifyNewPassword = verifyNewPasswordField.getText();

        // Verifying username and dob are not empty
        if(username.isEmpty()){
            errorMessageForgotPass.setText("Please provide Username");
            return false;
        }
        if(dateOfBirth.getValue() == null){
            errorMessageForgotPass.setText("Please provide Date of Birth");
            return false;
        }else{
            userDob = dateOfBirth.getValue().toString();
        }
        //verifying username and dob match a user in the db
        boolean validCred = database.validEmailDob(username,userDob);
        if(!validCred){
            errorMessageForgotPass.setText("Incorrect Credentials Username or DOB");
            return false;
        }
        //Checking the new password fields are not empty and match
        if(newPassword.isEmpty()){
            errorMessageForgotPass.setText("Please provide new password!");
            return false;
        }
        if (!validatePassword(newPassword)) {
            errorMessageForgotPass.setText(passwordErrorMsg);
            System.out.println(passwordErrorMsg);
            return false;
        }
        if(!(newPassword.equals(verifyNewPassword))){
            errorMessageForgotPass.setText("Passwords do not match!");
            return false;
        }
        boolean passwordUpdated = database.updatePassword(username, newPassword);
        if(passwordUpdated){
            errorMessageForgotPass.setText("Success!");
            return true;
        }else {
            errorMessageForgotPass.setText("Password Reset Failed");
            System.out.println("RESET FAILED");
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    /// resetPassword() reset new password                                              ///
    /// Input : None                                                                    ///
    /// Output: Boolean - true if password reset otherwise false                        ///
    /// Returns true if the password successfully changed in Database, false otherwise  ///
    ///////////////////////////////////////////////////////////////////////////////////////
    public boolean resetPassword() throws IOException{
        String newPassword = newPasswordField.getText();
        String verifyNewPassword = verifyNewPasswordField.getText();
        if(newPassword.isEmpty()){
            errorResetPassword.setText("Please provide new password!");
            return false;
        }
        if (!validatePassword(newPassword)) {
            errorResetPassword.setText(passwordErrorMsg);
            return false;
        }
        if(!newPassword.equals(verifyNewPassword)){
            errorResetPassword.setText("Passwords do not match!");
            return false;
        }
        String username = (curUser != null) ? curUser.getUsername() : null;
        if (username == null) {
            return false;
        }
        boolean passwordUpdated = database.updatePassword(username, newPassword);
        if(passwordUpdated){
            return true;
        }else {
            errorResetPassword.setText("Failed to update password!");
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    /// changeVisibility() show or hide the password in Login Page and Sign Up page     ///
    /// Input : None                                                                    ///
    /// Output: None                                                                    ///
    /// When you click the Show password button it is show the password otherwise hide  ///
    ///////////////////////////////////////////////////////////////////////////////////////
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


    ///////////////////////////////////////////////////////////////////////////////////////
    /// changeVisibility1() show or hide the password in Forgot Password page           ///
    /// Input : None                                                                    ///
    /// Output: None                                                                    ///
    /// When you click the Show password button it is show the password otherwise hide  ///
    ///////////////////////////////////////////////////////////////////////////////////////
    @FXML
    void changeVisibility1(MouseEvent event){
        ImageView clickedImage = (ImageView) event.getSource();
        if(clickedImage.getId().equals("id_eye_hide_1")){
            // Show password
            txt_field_password_1.setText(newPasswordField.getText());
            txt_field_password_1.setVisible(true);
            newPasswordField.setVisible(false);
            id_eye_show_1.setVisible(true);
            id_eye_hide_1.setVisible(false);
        }else if (clickedImage.getId().equals("id_eye_show_1")){
            // Hide the password
            newPasswordField.setText(txt_field_password_1.getText());
            txt_field_password_1.setVisible(false);
            newPasswordField.setVisible(true);
            id_eye_show_1.setVisible(false);
            id_eye_hide_1.setVisible(true);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// resetPasswordToSuccessMessage() Go to Success page                                                  ///
    /// Input : None                                                                                        ///
    /// Output: None                                                                                        ///
    /// Action: if the password is successfully changed, the screen will go to the Success Message page     ///
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    ///////////////////////////////////////////////////////////////////
    public void resetPassNotLoggedIn(ActionEvent event) throws IOException{
        if(checkandupdatePass()){
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SuccessMessages.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    /// SuccessMessagesToLogInPage() Go to Success page                                        ///
    /// Input : None                                                                           ///
    /// Output: None                                                                           ///
    /// Action: From Success Message page, the screen will go to the login page                ///
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void SuccessMessagesToLogInPage(ActionEvent event) throws IOException{
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LoginPage.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
    }

    //////////////////////////////////////////////////////////////////////////
    /// loginPageToSignUpPage() Go to Sign Up page                         ///
    /// Input : None                                                       ///
    /// Output: None                                                       ///
    /// Action: Brings user to sign up page when clicking sign up button   ///
    //////////////////////////////////////////////////////////////////////////
    public void loginPageToSignUpPage(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SignUpPage.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    ////////////////////////////////////////////////////////////////////////////////
    /// signUp() Create new Account                                              ///
    /// Input : User input: fullname, username, DOB, password,                   ///
    /// Output: True is successful else False                                    ///
    /// Action: Brings user to Login Page if successful, otherwise shows error   ///
    ////////////////////////////////////////////////////////////////////////////////
    public Boolean signUp() throws IOException {
        String fullname = txtfullname.getText();
        String username = txtusername.getText().toLowerCase();
        String dOB = dateOfBirth.getValue().toString();
        String password = txtpassword.getText();
        Boolean created = false;
        byte[] profileImageDate = null; // SET TO NULL INITIALLY, USER CAN PROVIDE IT LATER

        // User Req:
        // full name needs to be at least 2 characters.
        // username needs to be longer than 7 characters and contain "@bu.edu".
        // dOB needs to have a selection.
        // password needs to have at least 8 characters, 1 capital letter, 1 number and 1 symbol
        if(fullname.length() > 2 && username.length() > 7 && username.contains("@bu.edu") &&
            !dOB.isEmpty()) {
            boolean validPass = validatePassword(password);
            if (validPass) {
                created = database.createUser(fullname, username, password, dOB, profileImageDate);
                if (!created) {
                    errorMessageSignUp.setText("Failed to Create Account");
                    return created;
                }
                return created;

            } else {
                errorMessageSignUp.setText(passwordErrorMsg);
                return false;
            }
        } else {
            errorMessageSignUp.setText(
                "Fullname must be longer than 2. Username needs to contain \"@bu.edu\" and password longer than 3.");
            return false;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////
    /// validatePassword() Validates password                                    ///
    /// Input : String Password                                                  ///
    /// Output: True if valid password else False                                ///
    /// Verifies password meets all security requirements                        ///
    ////////////////////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////////////////////
    /// backToLogIn() Button that Navigates to login page                        ///
    /// Input : None                                                             ///
    /// Output: None                                                             ///
    /// Redirects to Log in page from Sign Up and Forgot Password                ///
    ////////////////////////////////////////////////////////////////////////////////
    public void backToLogIn(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LoginPage.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    /////////////////////////////////////////////////////////////////////////////////
    /// backToLogInAccountCreated() Navigates to login page after creating account///
    /// Input : None                                                              ///
    /// Output: None                                                              ///
    /// If sign up successful the page is redirected to log in                    ///
    /////////////////////////////////////////////////////////////////////////////////
    public void backToLogInAccountCreated(ActionEvent event) throws IOException {
        if (signUp()) {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LoginPage.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    /// loginPageToChatPage() Log In Button- Navigates to chat page              ///
    /// Input : None but uses serverIp                                           ///
    /// Output: None                                                             ///
    /// If login successful, page redirected to chat page                        ///
    ////////////////////////////////////////////////////////////////////////////////
    public void loginPageToChatPage(ActionEvent event) throws IOException {
        if (Login()) {
            ip = serverIP.getText();
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ChatPage.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }

    ///////////////////////////////////////////////////////////////////
    /// forgotPasswordLogIn() Go to Forgot Password                 ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// Action: used when user is not logged in... from Log in page ///
    ///////////////////////////////////////////////////////////////////
    public void forgotPasswordLogIn(ActionEvent event) throws IOException{
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ForgotPassword.fxml")));
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }
}