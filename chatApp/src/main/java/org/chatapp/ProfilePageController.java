package org.chatapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import static org.chatapp.SceneController.curUser;
import static org.chatapp.SceneController.database;
import java.nio.file.Path;
import java.nio.file.Paths;


//--------------------------------------------------------------------------------------------------------------------//
//                                                                                                                    //
// Class: ProfilePageController                                                                                       //
//                                                                                                                    //
//                                                                                                                    //
// Description:                                                                                                       //
//          The ProfilePageController class manages the user profile page of the chat application. It handles user    //
//          interactions related to updating profile details, managing profile images, and resetting passwords.       //
//                                                                                                                    //
//                                                                                                                    //
//--------------------------------------------------------------------------------------------------------------------//

public class ProfilePageController {
    private Stage stage;
    private Parent root;
    @FXML
    private Button button_form_1;
    @FXML
    private Button button_form_2;
    @FXML
    private TextField dateOfBirthTextField;
    @FXML
    private AnchorPane form_1;
    @FXML
    private AnchorPane form_2;
    @FXML
    private TextField fullNameTextField;
    @FXML
    private Circle imageView;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField newpasswordfield;
    @FXML
    private PasswordField verifynewpasswordfield;
    @FXML
    private TextField showpasswordtext;
    @FXML
    private CheckBox checkboxpassword;
    @FXML
    private Label errorMessageProfile;

    FileChooser fileChooser = new FileChooser(); // Allows users to select files (e.g., images) from their system.
    Path currRelativePath = Paths.get("");
    String currAbsolutePathString = currRelativePath.toAbsolutePath().toString();
    private final File imagePathsFile = new File(currAbsolutePathString + File.separator + "images.txt");
    private String selectedImagePath; // Stores the selected image path


    ////////////////////////////////////////////////////////////////////////////
    /// initialize()  sets initial values for text fields                    ///
    /// Input : None                                                         ///
    /// Output: None                                                         ///
    /// Loads user profile information (name, username, DOB) from the        ///
    //  database and sets initial values for text fields.                    ///
    ////////////////////////////////////////////////////////////////////////////
    @FXML
    private void initialize() throws FileNotFoundException {
        // THIS IS LOAD FOR IMAGES
        loadImages();

        // RETRIEVE USER PROFILE INFORMATION FROM DATABASE
        String fullName = database.getName(curUser.getUsername());
        String username = curUser.getUsername();
        String dateOfBirth = database.getDOB(curUser.getUsername());

        //SET INITIAL VALUE FOR TEXT FIELDS
        fullNameTextField.setText(fullName);                 // Editable in Scene Builder
        usernameTextField.setText(username);                 // not editable in Scene Builder
        dateOfBirthTextField.setText(dateOfBirth);           // not editable in Scene Builder
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// switchForm()  Switches between two pages                                                            ///
    /// Input : None                                                                                        ///
    /// Output: None                                                                                        ///
    /// Switches between two forms (form_1 and form_2) within the profile page based on button clicks.      ///
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void switchForm(ActionEvent event){
        if(event.getSource() == button_form_1){
            form_1.setVisible(true);
            form_2.setVisible(false);
        } else if (event.getSource() == button_form_2) {
            form_1.setVisible(false);
            form_2.setVisible(true);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// getImages() Opens a file chooser dialog to select a profile image from the user's system.                    ///
    /// Input : None                                                                                                 ///
    /// Output: None                                                                                                 ///
    /// Displays the selected image in a circular imageView and saves the image path to a text file (imagePathsFile) ///
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    void getImages() {
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageView.setFill(new ImagePattern(image));
            selectedImagePath = file.getPath(); // Store the selected image path
            // Save Images path to text file
            saveImagesPath(selectedImagePath);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// saveChanges() button updates user profile details (name, username, DOB) in the database.                     ///
    /// Input : None                                                                                                 ///
    /// Output: None                                                                                                 ///
    /// If a new profile image is selected, it reads the image data and updates the profile image in the database    ///
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    void saveChanges(){
        String fullName = fullNameTextField.getText();
        String username = usernameTextField.getText();
        String dateOfBirth = dateOfBirthTextField.getText();

        database.updateProfileDetails(username, fullName, dateOfBirth);

        // SAVE PROFILE IMAGE IF SELECTED FROM PC
        if(selectedImagePath != null){
            try{
                byte[] imageData = Files.readAllBytes(Paths.get(selectedImagePath));
                database.updateProfileImages(username, imageData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /// loadImages()                                                                                 ///
    /// Input : None                                                                                 ///
    /// Output: None                                                                                 ///
    /// Action: Loads the user's profile image from the database and displays it in the imageView.   ///
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private void loadImages() throws FileNotFoundException {
        byte[] imageData = database.getProfileImage(curUser.getUsername());
        if(imageData != null){
            Image image = new Image(new ByteArrayInputStream(imageData));
            imageView.setFill(new ImagePattern(image));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /// saveImagesPath()                                                                             ///
    /// Input : String path                                                                          ///
    /// Output: None                                                                                 ///
    /// Action: Saves the selected image path to a text file (imagePathsFile).                       ///
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private void saveImagesPath(String path){
        try(PrintWriter printWriter = new PrintWriter(imagePathsFile)) {
            printWriter.println(path);
            // Provide user feedback here, e.g., display a success message
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /// showPassword() button, password section in Profile page                                      ///
    /// Input : None                                                                                 ///
    /// Output: None                                                                                 ///
    /// Action: Toggles password visibility based on the state of a checkbox (checkboxpassword)      ///
    //          Allows users to view the entered password.                                           ///
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    void showPassword(ActionEvent event){
        if(checkboxpassword.isSelected()){
            showpasswordtext.setText(verifynewpasswordfield.getText());
            showpasswordtext.setVisible(true);
            verifynewpasswordfield.setVisible(false);
            return;
        }
        verifynewpasswordfield.setText(showpasswordtext.getText());
        verifynewpasswordfield.setVisible(true);
        showpasswordtext.setVisible(false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /// resetPasswordProfile() resets the password for the current user in Profile page              ///
    /// Input : None                                                                                 ///
    /// Output: Boolean - true if password reset otherwise false                                     ///
    /// Action: Updates the password in the database and displays appropriate                        ///
    //          error messages (errorMessageProfile) if needed.                                      ///
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean resetPasswordProfile() throws IOException{
        String newPassword = newpasswordfield.getText();
        String verifyNewPassword = verifynewpasswordfield.getText();
        if(newPassword.isEmpty()){
            errorMessageProfile.setText("Please provide new password!");
            return false;
        }
        if(!newPassword.equals(verifyNewPassword)){
            errorMessageProfile.setText("Passwords do not match!");
            return false;
        }

        // GET CURRENT LOGGED-IN USER:
        String username = curUser.getUsername();

        //UPDATE THE PASSWORD IN THE DATABASE:
        boolean passwordUpdated = database.updatePassword(username, newPassword);
        if(passwordUpdated){
            return true;
        }else {
            // FAILED TO UPDATE PASSWORD
            errorMessageProfile.setText("Failed to update password!");
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// SubmitProfileToSuccessMessage() Go to Success page                                               ///
    /// Input : None                                                                                     ///
    /// Output: None                                                                                     ///
    /// Action: if the password is successfully changed, the screen will go to the Success Message page  ///
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void SubmitProfileToSuccesMessage(ActionEvent event) throws IOException{
        if(resetPasswordProfile()){
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SuccessMessages.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /// backToChatPage(), previous button in Profile page, goes to Chat page                         ///
    /// Input : None                                                                                 ///
    /// Output: None                                                                                 ///
    /// Action: Navigates back to the chat page (ChatPage.fxml) from the profile page.               ///
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void backToChatPage(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ChatPage.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }
}
