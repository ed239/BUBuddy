package org.chatapp.network;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {

    private ServerSocket serverSocket;
    private Socket socket;
    private static final int portNumber = 6667;

    public Server (ServerSocket serverSocket) throws IOException {
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

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                socket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(socket);
                System.out.println(clientHandler.getUserName() + " connected");
                Thread thread = new Thread(clientHandler);
                thread.start();

            }
        }
        catch (IOException e) {
            closeServer();
        }
    }

    public void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(portNumber);
        Server server = new Server(serverSocket);
        server.startServer();


    }
}