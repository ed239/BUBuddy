package org.chatapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.invoke.StringConcatFactory;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;

import static org.chatapp.SceneController.curUser;
import static org.chatapp.SceneController.database;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProfilePageController {
    @FXML
    private TextField fullNameTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField dateOfBirthTextField;
    @FXML
    private TextField emailTextField;

    FileChooser fileChooser = new FileChooser();
    Path currRelativePath = Paths.get("");
    String currAbsolutePathString = currRelativePath.toAbsolutePath().toString();
//    System.out.println("Current absolute path is - " + currAbsolutePathString);
    private final File imagePathsFile = new File(currAbsolutePathString + File.separator + "images.txt");
    private String selectedImagePath; // Stores the selected image path
    @FXML
    private ImageView imageView;
    @FXML
    private void initialize() throws FileNotFoundException {
        // THIS IS LOAD FOR IMAGES
        loadImages();

        // RETRIEVE USER PROFILE INFORMATION FROM DATABASE
        String fullName = database.getName(curUser.getUsername());
        String username = curUser.getUsername();
        String dateOfBirth = database.getDOB(curUser.getUsername());
        String email = database.getEmail(curUser.getUsername());

        //SET INITIAL VALUE FOR TEXT FIELDS
        fullNameTextField.setText(fullName);
        usernameTextField.setText(username);
        dateOfBirthTextField.setText(dateOfBirth);
        emailTextField.setText(email);
    }

    @FXML
    void getImages() {
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
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
        String email = emailTextField.getText();

        boolean success = database.updateProfileDetails(username, fullName, dateOfBirth, email);
        // SAVE PROFILE IMAGE IF SELECTED FROM PC
        if(selectedImagePath != null){
            try{
                byte[] imageData = Files.readAllBytes(Paths.get(selectedImagePath));
//                String username = curUser.getUsername();
                database.updateProfileImages(username, imageData);
//                System.out.println("\nIMAGES CHANGES SAVED SUCCESSFULLY!\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(success){
            System.out.println("\nCHANGES SAVED SUCCESSFULLY!\n");
        }else {
            System.out.println("\nFAILED TO SAVE CHANGES\n");
        }
    }
    // Method to load images from saved paths
    private void loadImages() throws FileNotFoundException {
        byte[] imageData = database.getProfileImage(curUser.getUsername());
        if(imageData != null){
            Image image = new Image(new ByteArrayInputStream(imageData));
            imageView.setImage(image);
        }
    }
    private void saveImagesPath(String path){
        try(PrintWriter printWriter = new PrintWriter(imagePathsFile)) {
            printWriter.println(path);
            System.out.println("\nIMAGE PATH SAVED: " + path + "\n");
            // Provide user feedback here, e.g., display a success message
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void backToChatPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ChatPage.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }
}
