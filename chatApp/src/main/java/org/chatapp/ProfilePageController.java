package org.chatapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

import static org.chatapp.SceneController.curUser;
import static org.chatapp.SceneController.database;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProfilePageController {
    FileChooser fileChooser = new FileChooser();
    Path currRelativePath = Paths.get("");
    String currAbsolutePathString = currRelativePath.toAbsolutePath().toString();
//    System.out.println("Current absolute path is - " + currAbsolutePathString);
    private final File imagePathsFile = new File(currAbsolutePathString + "images.txt");
    private String selectedImagePath; // Stores the selected image path
    @FXML
    private ImageView imageView;
    @FXML
    private void initialize() throws FileNotFoundException {
        loadImages();
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
        if(selectedImagePath != null){
            String username = curUser.getUsername();
            database.updateProfileImages(username, selectedImagePath);
            System.out.println("Changes saved successfully");
            System.out.println("Changes saved successfully");
            System.out.println("Changes saved successfully\n");
        }else {
            System.out.println("No image selected to save changes.");
        }
    }
    // Method to load images from saved paths
    private void loadImages() throws FileNotFoundException {
        if (curUser.getProfileImagePath() != null) {
            Scanner scanner = new Scanner(SceneController.curUser.getProfileImagePath());
            while (scanner.hasNextLine()) {
                String imagesPath = scanner.nextLine();
                File file = new File(imagesPath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    selectedImagePath = imagesPath;
                    // Load only the first image for demonstration
                }
            }
            scanner.close();
        }
    }
    private void saveImagesPath(String path){
        try(PrintWriter printWriter = new PrintWriter(imagePathsFile)) {
            printWriter.println(path);
            System.out.println("\nImage path saved: " + path);
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
