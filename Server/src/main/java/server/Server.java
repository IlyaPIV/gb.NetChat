package server;

import constants.Command;

import javax.print.attribute.standard.Severity;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class Server {

    private  ServerSocket serverSocket;
    private  Socket clientSocket;
    private  final int PORT = 8189;

    private List<ClientHandler> clients;

    private AuthService authService;
    private SqliteAuthService sqlAuthService;

    private ExecutorService executorService;

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public Server() {

        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        sqlAuthService = new SqliteAuthService();

        //HomeWork4+++
        executorService = Executors.newCachedThreadPool();
        //HomeWork4---

        //HomeWork6+++
        try {

            LogManager logManager = LogManager.getLogManager();
            logManager.readConfiguration(new FileInputStream("Server/logging.properties"));

//            Handler fileHandler = new FileHandler("log_server_%d.txt", 10*1024, 10,true);
//            fileHandler.setFormatter(new SimpleFormatter());
//            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //HomeWork6---

        try {

            serverSocket = new ServerSocket(PORT);
            LOGGER.log(Level.INFO,"Server started");
            //System.out.println("Server started");

            while (true) {
                clientSocket = serverSocket.accept();
                LOGGER.log(Level.INFO,"Client connected");
                //System.out.println("Client connected");

                new ClientHandler(this,clientSocket);
            }


        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE,e.getMessage());
        } finally {

            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.log(Level.SEVERE,e.getMessage());
            }

            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.log(Level.SEVERE,e.getMessage());
            }

            sqlAuthService.disconnect();
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg, boolean system){
        String message = system? msg : String.format("%s > [%s]: %s",serverTime(), sender.getNickname(),msg);
        for (ClientHandler client:
             clients) {
            client.sendMsg(message);
        }
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder(Command.CLIENT_LIST);

        for (ClientHandler ch:
             clients) {
            sb.append(" ").append(ch.getNickname());
        }

        String msg = sb.toString();

        for (ClientHandler ch:
                clients) {
            ch.sendMsg(msg);
        }
    }

    public void privateMsg(ClientHandler sender, String getterNickname, String msg){
        ClientHandler getter = findGetter(getterNickname);
        if (getter==null) {
            String message = String.format("failed to send private message: user [%s] is not online",getterNickname);
            sender.sendMsg(message);
        } else {
            String messageToGetter = String.format("%s > private from [%s]: %s",serverTime(),sender.getNickname(),msg);
            String messageToSender = String.format("%s > private to [%s]: %s", serverTime(), getterNickname, msg);
            getter.sendMsg(messageToGetter);
            if (!sender.getNickname().equals(getterNickname)) sender.sendMsg(messageToSender);
        }
    }

    public ClientHandler findGetter(String getterNickname){

        for (ClientHandler client:
             clients) {
            if (client.getNickname().equals(getterNickname)) return client;
        }
        return null;
    }


    public boolean isLoginUsed(String login){
        for (ClientHandler ch:
             clients) {
            if (ch.getLogin().equals(login)) return true;
        }
        return false;
    }

    public void subscribe(ClientHandler ch) {
        String message = String.format("%s !!! user [%s] now is online...",serverTime(),ch.getNickname());
        broadcastMsg(ch,message, true);
        clients.add(ch);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler ch) {
        clients.remove(ch);
        String message = String.format("%s !!! user [%s] left our chat...",serverTime(), ch.getNickname());
        broadcastMsg(ch,message, true);
        broadcastClientList();
    }

    public AuthService getAuthService(){
        //return authService;
        return sqlAuthService;
    }

    private String serverTime(){
        Date currentTime = new Date();

        SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        return String.format("[%s]",formatForDateNow.format(currentTime));

    }

    public ExecutorService getExecutorService(){
        return executorService;
    }

    public Logger getLogger()
    {
        return LOGGER;
    }

}
