package org.chatapp.network;

import org.chatapp.SceneController;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class chatClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555; //you can change the port if you have errors

    private SceneController sceneController;


    public chatClient(SceneController sceneController) {
        this.sceneController = this.sceneController;
    }


    //Use this if running this file independently
//    public static void main(String[] args) {
//        try {
//            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
//            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
//            Scanner scanner = new Scanner(socket.getInputStream());
//
//            System.out.println("Client connected successfully!");
//
//            //incoming messages
//            new Thread(() -> {
//                try {
//                    while (scanner.hasNextLine()) {
//                        System.out.println(scanner.nextLine());
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//
//            // testing sending messages in the server
//            Scanner consoleInput = new Scanner(System.in);
//            while (true) {
//                String message = consoleInput.nextLine();
//                writer.println(message);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //Use this if running from BUBuddyApp.java
    public static void main() {
        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(socket.getInputStream());

            System.out.println("Client connected successfully!");

            //incoming messages
            new Thread(() -> {
                try {
                    while (scanner.hasNextLine()) {
                        System.out.println(scanner.nextLine());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // testing sending messages in the server
            Scanner consoleInput = new Scanner(System.in);
            while (true) {
                String message = consoleInput.nextLine();
                writer.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
