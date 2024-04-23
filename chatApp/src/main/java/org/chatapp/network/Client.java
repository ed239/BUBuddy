package org.chatapp.network;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import org.chatapp.ChatPageController;
import java.io.*;
import java.net.Socket;

//
// Class: Client
//
// Description:
//     This is a Client class in a "Server-Client socket".
//     This class creates a client. It runs in ClientManager.
//
public class Client {
    public Socket socket;                                  // Socket for Client
    private BufferedReader bufferedReader;                 // for reading message
    private BufferedWriter bufferedWriter;                 // for writing message
    private String userName;                               // Client's user name
    private final ObservableList<String> userNames = FXCollections.observableArrayList();         // save all usernames except your own

    ///////////////////////////////////////////////////////////////////
    /// Client(socket, userName) Constructor for client             ///
    /// Input : socket, userName                                    ///
    /// Output: None                                                ///
    /// Creates Client object with Socket instance and userName     ///
    ///////////////////////////////////////////////////////////////////
    public Client(Socket socket, String userName) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = userName;
            bufferedWriter.write(userName);               // writes down his name to other users
            bufferedWriter.newLine();
            bufferedWriter.flush();
            setUserNames();
        } catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    ///////////////////////////////////////////////////////////////////
    /// sendMessage(toUser, message) sends message to user          ///
    /// Input : toUser, message                                     ///
    /// Output: None                                                ///
    /// Allows client send messages to someone/toUser               ///
    ///////////////////////////////////////////////////////////////////
    public void sendMessage(String toUser, String message) {
        try {
            bufferedWriter.write(userName + " >:> " + toUser + " >:> " + message);    // writes his own name, to whom to send and a message
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    ///////////////////////////////////////////////////////////////////
    /// readMessage(vBox) read message from users                   ///
    /// Input : vBox                                                ///
    /// Output: None                                                ///
    /// Allows client read messages from users and populates        ///
    /// in vBox in Chat page.                                       ///
    ///////////////////////////////////////////////////////////////////
    public void readMessage(VBox vBox) {
        new Thread(() -> {
                String messageFromServer;
                String[] parts;
                while (socket.isConnected()) {
                    try {
                        messageFromServer = bufferedReader.readLine();                      // reads message from Server
                        if (messageFromServer.equals("addUser") || messageFromServer.equals("removeUser")) {  // if message is addUser or removeUser then update list
                            updateUserNames(messageFromServer + ":" + bufferedReader.readLine());
                        } else if (messageFromServer.contains(" >:> " + userName + " >:> ")) {          // If message is intended for this particular client, then show it on the chat page with the sender
                            parts = messageFromServer.split(" >:> ");
                            ChatPageController.addLabel(parts[2], vBox, true);
                        } else if (messageFromServer.contains(" >:> " + "Group Chat" + " >:> ")) {      // If message for group chat then show it in group chat
                            parts = messageFromServer.split(" >:> ");
                            ChatPageController.addLabel(parts[0] + ": " + parts[2], vBox, true);
                        }
                    } catch (IOException e) {
                        closeAll(socket, bufferedWriter, bufferedReader);
                        break;
                    }
                }
        }).start();
    }

    ///////////////////////////////////////////////////////////////////
    /// getUsernames() returns all userNames                        ///
    /// Input : None                                                ///
    /// Output: userNames                                           ///
    /// Getter for userNames                                        ///
    ///////////////////////////////////////////////////////////////////
    public ObservableList<String> getUsernames() {
        return userNames;
    }

    ///////////////////////////////////////////////////////////////////
    /// setUserNames() sets all new userNames                       ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// Method for getting all users from ClientHandler             ///
    ///////////////////////////////////////////////////////////////////
    public void setUserNames() {
        try {
            bufferedWriter.write("get_usernames\n");    // send a request to get the names of all users
            bufferedWriter.flush();
            String nameOfUser;
            while (!((nameOfUser = bufferedReader.readLine()) == null) && !nameOfUser.equals("end")) {   // add userNames until there is a null or end
                if (!nameOfUser.equals(userName)) {   // add all userNames except your own
                    userNames.add(nameOfUser);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ChatPageController.updateLocalChatList(getUsernames());      // update list view in Chat page
    }

    ///////////////////////////////////////////////////////////////////
    /// updateUserNames(message) add or delete user from list       ///
    /// Input : message                                             ///
    /// Output: None                                                ///
    /// Method updates userNames (add/remove)                       ///
    ///////////////////////////////////////////////////////////////////
    public void updateUserNames(String message) {
        String[] parts = message.split(":");
        String action = parts[0].trim();
        String userName = parts[1].trim();

        if (action.equals("addUser")) {  // if message addUser then add new userName
            userNames.add(userName);
        } else if (action.equals("removeUser")) { // if the message is removeUser, then remove the user from the list of users
            userNames.remove(userName);
        }
        ChatPageController.updateLocalChatList(getUsernames());   // update list view in Chat page
    }

    ///////////////////////////////////////////////////////////////////
    /// closeAll(socket, bufferedWriter, bufferedReader)            ///
    /// closes socket, bufferedWriter, and bufferedReader           ///
    /// Input : socket, bufferedWriter, bufferedReader              ///
    /// Output: None                                                ///
    /// Method closes everything                                    ///
    ///////////////////////////////////////////////////////////////////
    public void closeAll(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}