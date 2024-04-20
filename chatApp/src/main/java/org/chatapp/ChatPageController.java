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

//
// Class: ChatPageController
//
// Description:
//     This is a Chat Page Class that is used for all actions in the Main Chat and Local Chat
//     This class also contains redirections to the profile page and allows logging out
//
public class ChatPageController {
    private static ChatUser curUser = getCurUser();
    private static ChatUser toUserObj;
    private static Database database = getDb();

    public static String ip = getIP();


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

    ///////////////////////////////////////////////////////////////////
    /// initialize() sets requires vars for page                    ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// includes both tabs for the left panel and the two lists of  ///
    ///  users in the main chat and local chat lists (observers are ///
    ///  in this method                                             ///
    @FXML
    private void initialize() {
        curUser= getCurUser(); //this isn't very efficient -- have to fix
        String[] users = database.getAllChatUsersExceptCurrent(curUser);
        ObservableList<String> allChatUsersExceptCurrent = FXCollections.observableArrayList(users);
        FXCollections.sort(allChatUsersExceptCurrent, String.CASE_INSENSITIVE_ORDER);
        userListView.setItems(allChatUsersExceptCurrent);
        clientManager = ClientManager.getInstance(ip);
        client = clientManager.getClient();    // initializing Client socket for this user

        localListView.setItems(localChatList); // initializing the name of local chat users

        timeline = new Timeline(//to update message view for new messages every second
                new KeyFrame(Duration.seconds(1), event -> {
                    displayNewMessages(curUser.getId(), toUserObj.getId(),"");
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);

        /////////////////////////////////////////////////
        /// listener for selecting user to chat with  ///
        /////////////////////////////////////////////////
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

        /////////////////////////////////////////////////////
        /// listener for changing chat pages: main/local  ///
        /////////////////////////////////////////////////////

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

        ///////////////////////////////////////////////////////////////
        /// listener for selecting user to chat with in local chat  ///
        ///////////////////////////////////////////////////////////////
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
    ///////////////////////////////////////////////////////////////////
    /// getIP() gets ip from user input in login page               ///
    /// Input : None                                                ///
    /// Output: return IP address (String)                          ///
    /// Used to connect to server -- if empty will connect to local ///
    private static String getIP() {
        return SceneController.ip;
    }

    ///////////////////////////////////////////////////////////////////
    /// addLabel(messageFromUser, vBox, isSent)                     ///
    /// populates message from Server                               ///
    /// Input : messageFromUser, vBox, isSent                       ///
    /// Output: None                                                ///
    /// Populates message from Server into Client                   ///
    /// chat page(chatContainer)                                    ///
    ///////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////
    /// backToLogIn() log out button                                ///
    /// Input : None                                                ///
    /// Output: return current user (ChatUSer)                      ///
    /// logs user out and redirects back to Log In page             ///
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
    ///////////////////////////////////////////////////////////////////
    /// getCurUser() gets current user obj from login               ///
    /// Input : None                                                ///
    /// Output: return current user (ChatUSer)                      ///
    /// Used to connect to server -- if empty will connect to local ///
    public static ChatUser getCurUser() {
        return SceneController.curUser;
    }
    ///////////////////////////////////////////////////////////////////
    /// getDb() gets current user obj from login                    ///
    /// Input : None                                                ///
    /// Output: return database instance                            ///
    /// used to not create a new instance                           ///
    private static Database getDb() {
        return SceneController.database;
    }

    ///////////////////////////////////////////////////////////////////
    /// sendMessage() sends message to user                         ///
    /// Input : gets input from user input                          ///
    /// Output: None                                                ///
    /// If in local chat the message is sent through the client     ///
    /// and server  - in main chat the messsage is added to the db  ///
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
                    displayNewMessages(toId, curUser.getId(), messageToSend);
                    txtmessage.setText("");
                }
            }
        }
    }
    ///////////////////////////////////////////////////////////////////
    /// displayAllMessages() shows all messages (between users)     ///
    /// Input : ObjectID toId, fromId, String text                  ///
    /// Output: None                                                ///
    /// gets all messages and then displays them                    ///
    /// Auto updating on to see new messages that are sent/recieved ///
    public void displayAllMessages(ObjectId toId, ObjectId fromId, String text) {
        displayedMessages.clear();
        FindIterable<Document> messages = database.getMessagesBetweenUsers(toId, fromId);
        for (Document message : messages) {
            if (!displayedMessages.contains(message)) {
                displayMessage(message);
                displayedMessages.add(message);
            }
        }
        startDisplayingNewMessages();
        isAutoUpdating = true;
    }

    ///////////////////////////////////////////////////////////////////
    /// displayMessage() displays a single message                  ///
    /// Input : Document message                                    ///
    /// Output: None                                                ///
    /// displays message (orientation and color) based on cur user  ///
    /// and from ID - adds message to chat container                ///
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

    ///////////////////////////////////////////////////////////////////
    /// startDisplayingNewMessages() starts autoupdate of chat      ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// begins auto updating to see messages in real time           ///
    public void startDisplayingNewMessages() {
        timeline.play(); // Start the Timeline
    }

    ///////////////////////////////////////////////////////////////////
    /// stopDisplayingNewMessages() stops autoupdate of chat        ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// stop auto updating when new user is clicked/or chat changes ///
    public void stopDisplayingNewMessages() {
        timeline.stop(); // Stop the Timeline
    }

    ///////////////////////////////////////////////////////////////////
    /// updateLocalChatList(userNames) updates local chat users     ///
    /// Input : ObservableList<String> userNames                    ///
    /// Output: None                                                ///
    /// Updates (adds/removes) list of local chat users             ///
    ///////////////////////////////////////////////////////////////////
    public static void updateLocalChatList(ObservableList<String> userNames) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                localChatList.setAll(userNames);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    /// displayNewMessages() displays any new messages on screen             ///
    /// Input : ObjectId toId, fromId, String text                           ///
    /// Output: None                                                         ///
    /// after all messages displayed, any new sent/recieved are also shown   ///
    public void displayNewMessages(ObjectId toId, ObjectId fromId, String text) {
        FindIterable<Document> newMessages = database.getNewMessagesBetweenUsers(toId, fromId, lastDisplayedTimestamp);
            for (Document message : newMessages) {
                if (!displayedMessages.contains(message)) {
                    displayMessage(message);
                    displayedMessages.add(message);
                }
            }

    }

}
