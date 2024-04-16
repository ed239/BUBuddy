package org.chatapp.network;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import org.chatapp.ChatPageController;

import java.io.*;
import java.net.Socket;

public class Client {

    public Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;
    private final ObservableList<String> userNames = FXCollections.observableArrayList();

    public Client(Socket socket, String userName) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = userName;
            bufferedWriter.write(userName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            setUserNames();
        } catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    //sendMessage method for GUI ChatPageController
    public void sendMessage(String toUser, String message) {
        try {
            bufferedWriter.write(userName + " >:> " + toUser + " >:> " + message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    //readMessage method for GUI ChatPageController
    public void readMessage(VBox vBox) {
        new Thread(() -> {
                String messageFromServer;
                while (socket.isConnected()) {
                    try {
                        messageFromServer = bufferedReader.readLine();
                        if (messageFromServer.equals("addUser") || messageFromServer.equals("removeUser")) {
                            updateUserNames(messageFromServer + ":" + bufferedReader.readLine());
                        } else if (messageFromServer.contains(" >:> " + userName + " >:> ")) {
                            ChatPageController.addLabel(messageFromServer, vBox, true);
                        } else if (messageFromServer.contains(" >:> " + "Group Chat" + " >:> ")) {
                            ChatPageController.addLabel(messageFromServer, vBox, true);
                        }
                    } catch (IOException e) {
                        closeAll(socket, bufferedWriter, bufferedReader);
                        break;
                    }
                }
        }).start();
    }

    public ObservableList<String> getUsernames() {
        return userNames;
    }

    public void setUserNames() {
        try {
            bufferedWriter.write("get_usernames\n");
            bufferedWriter.flush();
            String nameOfUser;
            while (!((nameOfUser = bufferedReader.readLine()) == null) && !nameOfUser.equals("end")) {
                if (!nameOfUser.equals(userName)) {
                    userNames.add(nameOfUser);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUserNames(String message) {
        String[] parts = message.split(":");
        String action = parts[0].trim();
        String userName = parts[1].trim();

        if (action.equals("addUser")) {
            userNames.add(userName);
        } else if (action.equals("removeUser")) {
            userNames.remove(userName);
        }
        ChatPageController.updateLocalChatList(getUsernames());
    }

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