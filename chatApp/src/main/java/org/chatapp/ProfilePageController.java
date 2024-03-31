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


public class ProfilePageController {
    FileChooser fileChooser = new FileChooser();
    private final File imagePathsFile = new File("D:\\JAVA_CLASS\\BUBuddy\\chatApp\\src\\main\\resources\\img\\image_paths.txt");
    private String selectedImagePath; // Stores the selected image path


    @FXML
    private ImageView imageView;

    @FXML
    private void initialize(){
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
            //saveImagesPath(file.getPath());
        }
    }
    // Method to save changes when "Save Changes" button is clicked
    @FXML
    void saveChanges(){
        if(selectedImagePath != null){
            saveImagesPath(selectedImagePath);
            System.out.println("Changes saved successfully");
            System.out.println("Changes saved successfully");
            System.out.println("Changes saved successfully");
        }else {
            System.out.println("No image selected to save changes.");
        }
    }

    // Method to load images from saved paths
    private void loadImages(){
        try {
            Scanner scanner = new Scanner(imagePathsFile);
            while (scanner.hasNextLine()){
                String imagesPath = scanner.nextLine();
                File file = new File(imagesPath);
                if(file.exists()){
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    selectedImagePath = imagesPath;
                    break; // Load only the first image for demonstration
                }
            }
            scanner.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
    private void saveImagesPath(String path){
        try{
            FileWriter fileWriterwriter = new FileWriter(imagePathsFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriterwriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            System.out.println(path);
        }catch (IOException e){
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
