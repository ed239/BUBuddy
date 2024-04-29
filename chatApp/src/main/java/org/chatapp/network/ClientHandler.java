package org.chatapp.network;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

//
// Class: ClientHandler
//
// Description:
//     This is a ClientHandler class for "Server-Client socket".
//     This class collects clients. It runs in Server implements Runnable interface.
//
public class ClientHandler implements Runnable{
    private static final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();            // list for collecting all ClientHandlers
    private static final ArrayList<String> userNames = new ArrayList<>();                        // list for collecting all usernames
    private Socket socket;
    private BufferedReader bufferedReader;                                                       // for reading message
    private BufferedWriter bufferedWriter;                                                       // for writing message
    private String userName;                                                                     // clientHandler user name

    ///////////////////////////////////////////////////////////////////
    /// ClientHandler(socket) Constructor for ClientHandler         ///
    /// Input : socket                                              ///
    /// Output: None                                                ///
    /// Creates ClientHandler object with Socket, bufferedWriter,   ///
    /// bufferedReader, userName instances                          ///
    ///////////////////////////////////////////////////////////////////
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = bufferedReader.readLine();
            clientHandlers.add(this);
            userNames.add(userName);
            broadcastMessage("addUser");            // this message triggers updateUserNames method of the client
            broadcastMessage(userName);             // sending a new user to other users
            broadcastMessage("Server: " + userName + " connected");
        }
        catch (IOException e) {
            closeAll(socket, bufferedWriter, bufferedReader);
        }
    }

    ///////////////////////////////////////////////////////////////////
    /// run() Runnable interface method                             ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// Allows Client read message from other Clients               ///
    ///////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////
    /// populateUsers() gives Client access to all user names       ///
    /// Input : String message, String userToDelete                 ///
    /// Output: None                                                ///
    /// SProvides access to user names to other clients, it is      ///
    /// launched only by other methods                              ///
    ///////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////
    /// broadcastMessage() sends message to all users except current///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// Allows Clients to read message from other Clients           ///
    ///////////////////////////////////////////////////////////////////
    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.userName.equals(userName)) {
                    if (!message.isEmpty()) {
                        clientHandler.bufferedWriter.write(message);
                    }
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }
            catch (IOException e) {
                closeAll(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    /// removeClientHandler() removes client from list              ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// Removes a client from the static list and sends             ///
    /// message to other users that this client has left chat       ///
    ///////////////////////////////////////////////////////////////////
    public void removeClientHandler() {
        clientHandlers.remove(this);
        userNames.remove(userName);
        broadcastMessage("removeUser");
        broadcastMessage(userName);
        broadcastMessage("Server: " + userName + " left chat");
    }

    ///////////////////////////////////////////////////////////////////
    /// closeAll(socket, bufferedWriter, bufferedReader)            ///
    /// closes socket, bufferedWriter, and bufferedReader           ///
    /// Input : socket, bufferedWriter, bufferedReader              ///
    /// Output: None                                                ///
    /// Method closes everything                                    ///
    ///////////////////////////////////////////////////////////////////
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
