//----------------------------------------------------------------------------------------------------------------------
//
//  Server
//      Creates Server for Clients
//
//  Authors: Bolatbek Amiyev, Jacob Kustra, Eden Dubrovsky, Azamat Omirzak
//  Date: Spring 2024
//  Class: MET CS622
//  Issues: None known
//
//

package org.chatapp.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

//
// Class: Server
//
// Description:
//     This is a Server class in a "Server-Client socket".
//     This class creates a server for clients. It runs separately.
//
public class Server {

    private final ServerSocket serverSocket;                       // ServerSocket for receiving clients
    private static final int portNumber = 6667;                    // Port number on which Server will listen

    ///////////////////////////////////////////////////////////////////
    /// Server(serverSocket) Constructor for server                 ///
    /// Input : ServerSocket                                        ///
    /// Output: None                                                ///
    /// Creates Server object with ServerSocket instance            ///
    ///////////////////////////////////////////////////////////////////
    public Server (ServerSocket serverSocket)  {
        System.out.println("server started");
        this.serverSocket = serverSocket;
        InetAddress ip;
        try {

            ip = InetAddress.getLocalHost();
            System.out.println("Current IP address : " + ip.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();

        }
    }

    ///////////////////////////////////////////////////////////////////
    /// startServer() Starts the server to receive clients          ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// The server will accept all new client sockets               ///
    ///////////////////////////////////////////////////////////////////
    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {   // will accept clients until the server socket is closed
                Socket socket = serverSocket.accept();  // accept new client socket

                ClientHandler clientHandler = new ClientHandler(socket);   // creates new ClientHandler for this client
                Thread thread = new Thread(clientHandler);                 // create new thread for ClientHandler
                thread.start();                                            // start thread to handle client
            }
        }
        catch (IOException e) {
            closeServer();                                                 // close server in case of exception
        }
    }

    ///////////////////////////////////////////////////////////////////
    /// closeServer() Closes the server socket                      ///
    /// Input : None                                                ///
    /// Output: None                                                ///
    /// The server will stop working                                ///
    ///////////////////////////////////////////////////////////////////
    public void closeServer() {
        try {
            if (serverSocket != null) {  // If there is a server socket, then close it
                serverSocket.close();
            }
        }
        catch (IOException e) {
            e.getLocalizedMessage();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(portNumber);         // creates a new Server socket using the specified port number
        Server server = new Server(serverSocket);                         // creates a new Server instance with Server socket
        server.startServer();                                             // start server to accept client sockets
    }
}