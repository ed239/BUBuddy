package org.chatapp;


import org.chatapp.network.Client;
import java.io.IOException;
import java.net.Socket;


public class ClientManager {
    private static ClientManager instance;
    private Socket socket;
    private Client client;
    private final int portNumber = 6667;

    private ClientManager() {
        try {
            socket = new Socket("localhost", portNumber);
            client = new Client(socket, SceneController.curUser.getName());
        }catch (IOException e) {
            System.out.println("Couldn't connect to the Server");
        }
    }

    public static synchronized ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

    public Client getClient() {
        return client;
    }
}

