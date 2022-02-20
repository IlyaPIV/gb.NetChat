package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private  ServerSocket serverSocket;
    private  Socket clientSocket;
    private  final int PORT = 8189;

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

    public void broadcastMsg(ClientHandler sender, String msg, boolean system){
        String message = system? msg : String.format("[%s]: %s",sender.getNickname(),msg);
        for (ClientHandler client:
             clients) {
            client.sendMsg(message);
        }
    }

    public void privateMsg(ClientHandler sender, String getterNickname, String msg){
        ClientHandler getter = findGetter(getterNickname);
        if (getter==null) {
            String message = String.format("failed to send private message: user [%s] is not online",getterNickname);
            sender.sendMsg(message);
        } else {
            String messageToGetter = String.format("private from [%s]: %s",sender.getNickname(),msg);
            String messageToSender = String.format("private to [%s]: %s", getterNickname, msg);
            getter.sendMsg(messageToGetter);
            sender.sendMsg(messageToSender);
        }
    }

    public ClientHandler findGetter(String getterNickname){

        for (ClientHandler client:
             clients) {
            if (client.getNickname().equals(getterNickname)) return client;
        }
        return null;
    }

    public void subscribe(ClientHandler ch) {
        String message = String.format(">>> user [%s] now is online...",ch.getNickname());
        broadcastMsg(ch,message, true);
        clients.add(ch);
    }

    public void unsubscribe(ClientHandler ch) {
        clients.remove(ch);
        String message = String.format(">>> user [%s] left our chat...",ch.getNickname());
        broadcastMsg(ch,message, true);
    }

    public AuthService getAuthService(){
        return authService;
    }
}
