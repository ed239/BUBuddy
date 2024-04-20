package org.chatapp;



import org.chatapp.network.Client;

import java.io.IOException;
import java.net.Socket;



public class ClientManager {
    private static ClientManager instance;
    private Socket socket;
    private Client client;
    private final int portNumber = 6667;

    private ClientManager(String ip) {
        try {
            if (ip == null){
                System.out.println("Connected to LocalHost");
                socket = new Socket("localhost", portNumber);
            }
            else{
                System.out.println("Connected with IP Address");
                socket = new Socket(ip, portNumber);
            }
            client = new Client(socket, SceneController.curUser.getName());
        }catch (IOException e) {
            System.out.println("Couldn't connect to the Server");
        }
    }

    public static synchronized ClientManager getInstance(String ip) {
        if (instance == null) {
            instance = new ClientManager(ip);
        }
        return instance;
    }

    public Client getClient() {
        return client;
    }
}

