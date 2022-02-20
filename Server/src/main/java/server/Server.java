package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private  ServerSocket serverSocket;
    private  Socket clientSocket;
    private  final int PORT = 8189;

    private static DataInputStream in;
    private static DataOutputStream out;

    private List<ClientHandler> clients;

    private AuthService authService;

    public Server() {

        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();


        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("Client connected");

                new ClientHandler(this,clientSocket);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("[%s]: %s",sender.getNickname(),msg);
        for (ClientHandler client:
             clients) {
            client.sendMsg(message);
        }
    }

    public void subscribe(ClientHandler ch) {
        clients.add(ch);
    }

    public void unsubscribe(ClientHandler ch) {
        clients.remove(ch);
    }

    public AuthService getAuthService(){
        return authService;
    }
}
