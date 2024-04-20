package org.chatapp.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class chatServer {

    private static final int PORT = 5555;
    private static Set<PrintWriter> clients = new HashSet<>();

    //Use this if running independently
//    public static void main(String[] args) {
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            System.out.println("Server started on port " + PORT);
//
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("Client connected: " + clientSocket);
//
//                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
//                clients.add(writer);
//
//                new Thread(new ClientHandler(clientSocket, writer)).start();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    //Use this if running from BUBuddyApp.java
    public static void main() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clients.add(writer);

                new Thread(new ClientHandler(clientSocket, writer)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private Scanner scanner;

        public ClientHandler(Socket socket, PrintWriter writer) {
            this.clientSocket = socket;
            this.writer = writer;

            try {
                this.scanner = new Scanner(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (scanner.hasNextLine()) {
                        String message = scanner.nextLine();
                        System.out.println("Client says: " + message);
                        broadcast(message);
                    }
                }
//            } catch (IOException e) {
//                e.printStackTrace();
            } finally {
                // Client disconnected
                clients.remove(writer);
                System.out.println("Client disconnected: " + clientSocket);
            }
        }

        private void broadcast(String message) {
            for (PrintWriter client : clients) {
//                client.println(message);
                if (client != writer) {
                    client.println("Server says: " + message);
                }
            }
        }
    }
}
