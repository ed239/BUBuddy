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

    FileChooser fileChooser = new FileChooser();
    Path currRelativePath = Paths.get("");
    String currAbsolutePathString = currRelativePath.toAbsolutePath().toString();
    private final File imagePathsFile = new File(currAbsolutePathString + File.separator + "images.txt");
    private String selectedImagePath; // Stores the selected image path

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

    // SWITCH FORM_1 AND FORM_2 IN PROFILE PAGE
    public void switchForm(ActionEvent event){
        if(event.getSource() == button_form_1){
            form_1.setVisible(true);
            form_2.setVisible(false);
        } else if (event.getSource() == button_form_2) {
            form_1.setVisible(false);
            form_2.setVisible(true);
        }
    }

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

    // Method to save changes when "Save Changes" button is clicked
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

    // Method to load images from saved paths
    private void loadImages() throws FileNotFoundException {
        byte[] imageData = database.getProfileImage(curUser.getUsername());
        if(imageData != null){
            Image image = new Image(new ByteArrayInputStream(imageData));
            imageView.setFill(new ImagePattern(image));
        }
    }

    private void saveImagesPath(String path){
        try(PrintWriter printWriter = new PrintWriter(imagePathsFile)) {
            printWriter.println(path);
            // Provide user feedback here, e.g., display a success message
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // CHANGE THE VISIBILITY TO THE PASSWORD BY CLICKING SHOW-PASSWORD BUTTON
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

     //RESET PASSWORD IN PROFILE PAGE
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

     //RESET PASSWORD IN PROFILE PAGE
    public void SubmitProfileToSuccesMessage(ActionEvent event) throws IOException{
        if(resetPasswordProfile()){
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SuccessMessages.fxml")));
            stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        }
    }

    public void backToChatPage(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ChatPage.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }
}
