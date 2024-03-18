package org.chatapp;

import java.io.IOException;
import java.util.Objects;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.chatapp.com.mongodb.Database;
public class ChatPageController {

  private static ChatUser curUser = getCurUser();

  private static Database database = getDb();



  @FXML
  private ListView<String> userListView;

  @FXML
  private void initialize() {
//    String[] users = {curUser.getName(), "Contact 2", "Contact 3", "Contact 4"};
    String[] users = database.getAllChatUsersExceptCurrent(curUser);
    userListView.getItems().addAll(users);
    System.out.println(userListView);
  }

  public void backToLogIn(ActionEvent event) throws IOException {
    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LogInPage.fxml")));
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getScene().setRoot(root);
    stage.show();
  }

  public static ChatUser getCurUser() {
    return SceneController.curUser;
  }
  private static Database getDb() {
    return SceneController.database;
  }

}
