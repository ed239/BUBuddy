package org.chatapp;


import java.io.IOException;
import java.util.*;
import com.mongodb.client.FindIterable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.util.Duration;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.chatapp.com.mongodb.Database;
import org.chatapp.network.Client;

public class ChatPageController {
    private static ChatUser curUser = getCurUser();
    private static ChatUser toUserObj;
    private static Database database = getDb();

    @FXML
    private ListView<String> userListView;

    @FXML
    private ListView<String> localListView;

    @FXML
    private Label toUser;

    @FXML
    private TabPane chatTabs;

    @FXML
    private Tab localChatTab;

    @FXML
    private Tab mainChatTab;

    @FXML
    private TextArea txtmessage;

    @FXML
    private VBox chatContainer;
    @FXML
    private ScrollPane sp_local;
    private List<Document> displayedMessages = new ArrayList<>();
    private Date lastDisplayedTimestamp;
    private ClientManager clientManager;
    private Client client;

    private boolean isLocalChat = false; //for separating send button between main chat and Local group chat
    private static final ObservableList<String> localChatList = FXCollections.observableArrayList();
    private boolean isMainChat = false;
    private boolean isAutoUpdating = false;
    private Timeline timeline;


    @FXML
    private void initialize() {
        curUser= getCurUser(); //this isn't very efficient -- have to fix
        String[] users = database.getAllChatUsersExceptCurrent(curUser);
        ObservableList<String> allChatUsersExceptCurrent = FXCollections.observableArrayList(users);
        FXCollections.sort(allChatUsersExceptCurrent, String.CASE_INSENSITIVE_ORDER);
        userListView.setItems(allChatUsersExceptCurrent);

        clientManager = ClientManager.getInstance();
        client = clientManager.getClient();

        localListView.setItems(localChatList);

        timeline = new Timeline(//to update message view for new messages every second
                new KeyFrame(Duration.seconds(1), event -> {
                    displayNewMessages(curUser.getId(), toUserObj.getId());
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);

        userListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {
                if(isAutoUpdating){
                    stopDisplayingNewMessages();
                    isAutoUpdating = false;
                }
                toUser.setText(newValue);
                toUserObj = database.getChatUser(newValue);
                chatContainer.getChildren().clear();
                displayAllMessages(curUser.getId(), toUserObj.getId(),"");
                isLocalChat = false;  //for separating send button between main chat and Local group chat
                isMainChat = true;
            }
        });

        chatTabs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
                chatContainer.getChildren().clear();
                localListView.getSelectionModel().clearSelection();      //clears ListView selection
                userListView.getSelectionModel().clearSelection();   //clears ListView selection
                try {
                    if(newTab.equals(localChatTab)) {
                        client.readMessage(chatContainer);
                        toUser.setText("Local Chat");
                    } else if (newTab.equals(mainChatTab)) {
                        toUser.setText("Main Chat");
                    }
                }
                catch (NullPointerException e) {
                }
            }
        });

        // makes Local chats menu operate like main chat user selection listener
        localListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {
                chatContainer.getChildren().clear();
                toUser.setText(newValue);
                isLocalChat = true;      //for separating send button between main chat and Local group chat
                isMainChat = false;
            }});

        //for scrolling down when new messages come
        chatContainer.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp_local.setVvalue((Double) newValue);
            }
        });
    }

    public static void addLabel(String messageFromUser, VBox vBox, boolean isSent) {
        Label messageLabel = new Label(messageFromUser);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(0.3 * vBox.getWidth());

        HBox hBox = new HBox(messageLabel);
        hBox.setPadding(new Insets(10, 40, 10, 40));
        hBox.setMinWidth(Label.USE_PREF_SIZE);
        hBox.setMinHeight(Label.USE_PREF_SIZE);
        hBox.setAlignment(isSent ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        String backgroundColor = isSent ? "#FFCCCC" : "#E0E0E0";
        String borderRadius = "12px";
        messageLabel.setStyle("-fx-background-color: " + backgroundColor + ";" + "-fx-background-radius: " + borderRadius + ";" + "-fx-padding: 10px;");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBox.getChildren().add(hBox);
                hBox.setMinWidth(vBox.getWidth());
                hBox.setMaxWidth(vBox.getWidth());
            }
        });
    }

    public void backToLogIn(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LogInPage.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public void goToProfile(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ProfilePage.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.show();
    }

    public static ChatUser getCurUser() {
        return SceneController.curUser;
    }

    private static Database getDb() {
        return SceneController.database;
    }

    public void sendMessage(ActionEvent event) throws IOException {
        String userName = toUser.getText();
        String messageToSend = txtmessage.getText();
        if (!messageToSend.isEmpty()) {
            if (isLocalChat) {
                addLabel(messageToSend, chatContainer, false);
                client.sendMessage(userName, messageToSend);
                txtmessage.clear();
            }
            else if (isMainChat) {
                ObjectId toId = toUserObj.getId();
                Date currentTime = new Date();
                boolean done = database.addNewmessage(toId, curUser.getId(), messageToSend, currentTime);
                if (done) {
                    displayNewMessages(toId, curUser.getId());
                    txtmessage.setText("");
                }
            }
        }
    }

    public void displayAllMessages(ObjectId toId, ObjectId fromId, String text) {
        displayedMessages.clear();
        FindIterable<Document> messages = database.getMessagesBetweenUsers(toId, fromId);
        for (Document message : messages) {
            if (!displayedMessages.contains(message)) {
                displayMessage(message);
                displayedMessages.add(message);
            }
        }
        startDisplayingNewMessages(toId,fromId);
        isAutoUpdating = true;
    }

    private void displayMessage(Document message) {
        String text = message.getString("text");
        ObjectId senderId = message.getObjectId("fromId");
        lastDisplayedTimestamp = message.getDate("timestamp");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(0.3 * chatContainer.getWidth());

        HBox messageContainer = new HBox(messageLabel);
        messageContainer.setPadding(new Insets(10, 40, 10, 40));
        messageContainer.setMinWidth(Label.USE_PREF_SIZE);
        messageContainer.setMinHeight(Label.USE_PREF_SIZE);
        messageContainer.setAlignment(senderId.equals(curUser.getId()) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        String backgroundColor = senderId.equals(curUser.getId()) ? "#FFCCCC" : "#E0E0E0";
        String borderRadius = "12px";
        messageLabel.setStyle("-fx-background-color: " + backgroundColor + ";" + "-fx-background-radius: " + borderRadius + ";" + "-fx-padding: 10px;");

        chatContainer.getChildren().add(messageContainer);
        messageContainer.setMinWidth(chatContainer.getWidth());
        messageContainer.setMaxWidth(chatContainer.getWidth());
    }

    public void startDisplayingNewMessages(ObjectId toId, ObjectId fromId) {
        timeline.play(); // Start the Timeline
    }

    public void stopDisplayingNewMessages() {
        timeline.stop(); // Stop the Timeline
    }

    public static void updateLocalChatList(ObservableList<String> userNames) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                localChatList.setAll(userNames);
            }
        });
    }

    public void displayNewMessages(ObjectId toId, ObjectId fromId) {
        FindIterable<Document> newMessages = database.getNewMessagesBetweenUsers(toId, fromId, lastDisplayedTimestamp);
        for (Document message : newMessages) {
            if (!displayedMessages.contains(message)) {
                displayMessage(message);
                displayedMessages.add(message);
            }
        }
    }

}
