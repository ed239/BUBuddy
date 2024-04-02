package org.chatapp.network;
import org.chatapp.ChatPageController;
import org.chatapp.SceneController;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("Server: " + userName + " connected");
        }
        catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public void run() {
        String messageFromUser;

        while (socket.isConnected()) {
            try {
                messageFromUser = bufferedReader.readLine();
                broadcastMessage(messageFromUser);
            }
            catch (IOException e) {
                closeAll(socket, bufferedWriter, bufferedReader);
                break;
            }
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.userName.equals(userName)) {
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }
            catch (IOException e) {
                closeAll(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("Server: " + userName + " left chat");
    }

    public void closeAll(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClientHandler();
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
