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

public class ChatPageController {

  private static ChatUser curUser = getCurUser();

  @FXML
  private ListView<String> userListView;

  @FXML
  private void initialize() {
    String[] users = {"Contact 1", "Contact 2", "Contact 3", "Contact 4"};
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

}
