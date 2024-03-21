package org.chatapp;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import com.mongodb.client.FindIterable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.chatapp.com.mongodb.Database;
public class ChatPageController {

  private static ChatUser curUser = getCurUser();

  private static ChatUser toUserObj;

  private static Database database = getDb();



  @FXML
  private ListView<String> userListView;

  @FXML
  private ListView<String> secretListView;

  @FXML
  private Label toUser;

  @FXML
  private TabPane chatTabs;

  @FXML
  private Tab secretChatTab;

  @FXML
  private Tab mainChatTab;

  @FXML
  private TextArea txtmessage;

  @FXML
  private VBox chatContainer;



  @FXML
  private void initialize() {
//    String[] users = {curUser.getName(), "Contact 2", "Contact 3", "Contact 4"};
    curUser= getCurUser(); //this isnt very efficient -- have to fix
    String[] users = database.getAllChatUsersExceptCurrent(curUser);
    ObservableList<String> allChatUsersExceptCurrent = FXCollections.observableArrayList(users);
    userListView.setItems(allChatUsersExceptCurrent);

    // tested to see if Listview would become scrollable with a lot of users + show difference
    // in secret chat and main chat
    String[] temp = {"Mock 1", "Mock 2", "Mock 3", "Mock 4", "Mock 5", "Mock 6", "Mock 7", "Mock 8",
        "Mock 9", "Mock 10", "Mock 11", "Mock 12", "Mock 13", "Mock 14", "Mock 15", "Mock 16",
        "Mock 17", "Mock 18", "Mock 19", "Mock 20",
        "Mock 21", "Mock 22", "Mock 23", "Mock 24"};
    secretListView.getItems().addAll(temp);


    userListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
      @Override
      public void changed(ObservableValue observable, String oldValue, String newValue) {
        toUserObj = database.getChatUser(newValue);
        chatContainer.getChildren().clear();
        displayAllMessage(curUser.getId(), toUserObj.getId(),"");
        toUser.setText(newValue);
      }
    });

    // makes secret chats menu operate like main chat user selection listener
//    secretListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
//      @Override
//      public void changed(ObservableValue observable, String oldValue, String newValue) {
//        toUser.setText(newValue);
//      }
//    });




    chatTabs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
      @Override
      public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
        if(newTab.equals(secretChatTab)) {
          System.out.print(secretChatTab.isSelected());
          toUser.setText("Secret Chat");
        } else if (newTab.equals(mainChatTab)) {
          toUser.setText("Main Chat");
        }
      }
    });


  }

  public void backToLogIn(ActionEvent event) throws IOException {
    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LogInPage.fxml")));
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getScene().setRoot(root);
    stage.show();
  }

  public static ChatUser getCurUser() {
    System.out.println("CHAT PAGE getCurUser");
    System.out.println(SceneController.curUser);
    return SceneController.curUser;
  }
  private static Database getDb() {
    return SceneController.database;
  }
  public void sendMessage(ActionEvent event) throws IOException {
    ObjectId toId = toUserObj.getId();
    String text = txtmessage.getText();
    Date currentTime = new Date();
    if(text.length() > 1) {
      boolean done = database.addNewmessage(toId, curUser.getId(), text, currentTime);
      if (done) {
        displayNewMessage(toId, curUser.getId(),text);
        System.out.println("Message added successfully.");
        txtmessage.setText("");
      } else {
        System.out.println("Failed to add message.");
      }
    }
  }

  public void displayAllMessage(ObjectId toId, ObjectId fromId, String text) {
    FindIterable<Document> messages = database.getMessagesBetweenUsers(toId, fromId, text);
    for (Document message : messages) {
      String text2 = message.getString("text");
      ObjectId senderId = message.getObjectId("fromId");

      Label messageLabel = new Label(text2);
      messageLabel.setWrapText(true);
      messageLabel.setMaxWidth(200); // Adjust according to your needs

      HBox messageContainer = new HBox(messageLabel);
//      messageContainer.setAlignment(senderId.equals(curUser.getId()) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
//      String color = senderId.equals(curUser.getId()) ? "#FFCCCC" : "#E0E0E0";
//      messageContainer.setStyle("-fx-background-color: " + color + "; -fx-max-width: 200px;");
      messageContainer.setAlignment(senderId.equals(curUser.getId()) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
//      messageContainer.setStyle("-fx-background-color: " + (senderId.equals(curUser.getId()) ? "#ff9999" : "#d9d9d9") + "; -fx-max-width: 200px;");
      chatContainer.getChildren().add(messageContainer);
    }
  }

  public void displayNewMessage(ObjectId toId, ObjectId fromId, String text){

  }

}
