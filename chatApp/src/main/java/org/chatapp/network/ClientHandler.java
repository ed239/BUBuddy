package org.chatapp.network;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class ClientHandler implements Runnable{
    private static final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private static final ArrayList<String> userNames = new ArrayList<>();
    private Socket socket;
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
            userNames.add(userName);
            broadcastMessage("addUser");
            broadcastMessage(userName);
            broadcastMessage("Server: " + userName + " connected");
        }
        catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    public static ArrayList<String> getUserNames() {
        return userNames;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public void run() {
        String messageFromUser;
        populateUsers("start", null);
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

    public void populateUsers(String message, String userToDelete) {
        try {
            String request = bufferedReader.readLine();
            if (request.equals("get_usernames") || message.equals("delete")) {
                if (request.equals("get_usernames")) {
                    bufferedWriter.write("Group Chat\n");
                    for (String username : userNames) {
                        bufferedWriter.write(username + "\n");
                    }
                    bufferedWriter.write("end\n");
                    bufferedWriter.flush();
                }
                else {
                    bufferedWriter.write(message + "\n");
                    bufferedWriter.write(userToDelete + "\n");
                    bufferedWriter.write("end\n");
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        userNames.remove(userName);
        broadcastMessage("removeUser");
        broadcastMessage(userName);
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
