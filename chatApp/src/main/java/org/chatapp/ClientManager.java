package org.chatapp;


import org.chatapp.network.Client;
import java.io.IOException;
import java.net.Socket;

//
// Class: ClientManager
//
// Description:
//     This class manages creation of a Client instance and ensures that only one ClientManager instance exists.
//      Uses Singleton design pattern.
//
public class ClientManager {
    private static ClientManager instance;       // singleton instance
    private Client client;                       // client instance

    ///////////////////////////////////////////////////////////////////
    /// ClientManager(ip) Constructor to initialize ClientManager   ///
    /// Input : Ip address to connect to Server                     ///
    /// Output: None                                                ///
    /// Initialize ClientManager with specified IP address          ///
    ///////////////////////////////////////////////////////////////////
    private ClientManager(String ip) {
        try {
            Socket socket;
            int portNumber = 6667;
            if(ip.isEmpty()) {                      // if IP address not specified(is empty) then connect to localhost
                System.out.println("Connected to LocalHost");
                socket = new Socket("localhost", portNumber);
            }else {                                 // else connect to the specified IP address
                System.out.println("Connected with IP Address");
                socket = new Socket(ip, portNumber);
            }
            client = new Client(socket, SceneController.curUser.getName());  // create a new Client instance
        }catch (IOException e) {
            System.out.println("Couldn't connect to the Server");    // print error message if connection fails
        }
    }

    ///////////////////////////////////////////////////////////////////
    /// getInstance(ip) static method to get singleton instance of  ///
    /// ClientManager                                               ///
    /// Input : Ip address to connect to Server                     ///
    /// Output: instance                                            ///
    /// Initialize ClientManager with specified IP address          ///
    /// and returns instance                                        ///
    ///////////////////////////////////////////////////////////////////
    public static synchronized ClientManager getInstance(String ip) {
        if (instance == null) {  // if instance doesn't exist create new one
            instance = new ClientManager(ip);
        }
        return instance;
    }
    ///////////////////////////////////////////////////////////////////
    /// getClient() getter for client                               ///
    /// Input : None                                                ///
    /// Output: client                                              ///
    /// Returns client                                              ///
    ///////////////////////////////////////////////////////////////////
    public Client getClient() {
        return client;
    }
}

