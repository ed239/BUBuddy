package org.chatapp.network;
import javafx.scene.layout.VBox;
import org.chatapp.ChatPageController;

import java.io.*;
import java.net.Socket;

public class Client {

    public Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;

    public Client(Socket socket, String userName) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = userName;
            bufferedWriter.write(userName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    //sendMessage method for GUI ChatPageController
    public void sendMessage(String message) {
        try {
            bufferedWriter.write(userName + ": " + message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    //readMessage method for GUI ChatPageController
    public void readMessage(VBox vBox) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroupChat;
                while (socket.isConnected()) {
                    try {
                        messageFromGroupChat = bufferedReader.readLine();
                        ChatPageController.addLabel(messageFromGroupChat, vBox, true);
                    } catch (IOException e) {
                        closeAll(socket, bufferedWriter, bufferedReader);
                        break;
                    }
                }
            }
        }).start();
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