package server;

import constants.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickname;
    private String login;
    private Logger serverLogger;

    public ClientHandler(Server server, Socket socket) {


        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            serverLogger = server.getLogger();


            server.getExecutorService().execute(()->{
                try {

                    socket.setSoTimeout(120000);

                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                sendMsg(Command.END);
                                break;
                            }

                            if (str.startsWith(Command.AUTH+" ")) {
                                String[] token = str.split(" ",3);
                                if (token.length < 3) {
                                    continue;
                                }
                                String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1],token[2]);
                                login = token[1];
                                if (newNick != null) {
                                    if (!server.isLoginUsed(login)) {
                                        nickname = newNick;
                                        sendMsg(Command.AUTH_OK+" " + nickname);
                                        authenticated = true;
                                        server.subscribe(this);
                                        serverLogger.log(Level.FINE, login + " - успешная авторизация пользователя");
                                        break;
                                    } else {
                                        sendMsg("Эта учётная запись уже используется.");
                                        serverLogger.log(Level.FINE, login + " - учетная запись уже используется. Вход не выполнен");
                                    }
                                } else {
                                    sendMsg("Логин / пароль не верны");
                                    serverLogger.log(Level.FINE, login + " - ошибка входа: логин или пароль не верны.");
                                }
                            }

                            if (str.startsWith(Command.REG)) {
                                String[] token = str.split(" ");
                                if (token.length<4) {
                                    continue;
                                }
                                if (server.getAuthService().registration(token[1],token[2],token[3])){
                                    sendMsg(Command.REG_OK);
                                } else {
                                    sendMsg(Command.REG_FAIL);
                                }
                            }
                        }
                    }

                    socket.setSoTimeout(0);

                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.equals(Command.END)) {
                            sendMsg(Command.END);
                            break;
                        }

                        if (str.startsWith(Command.UPDATE_NICK)) {
                            String[] token = str.split(" ", 2);
                            if (server.getAuthService().updateNickname(login,token[1])) {
                                sendMsg("Ник успешно изменён");
                                serverLogger.log(Level.FINE, login + " - никнейм успешно изменён");
                                nickname = token[1];
                                sendMsg(Command.UPDATE_OK+" "+nickname);
                                server.broadcastClientList();

                            } else {
                                sendMsg("Ошибка изменения никнейма");
                                serverLogger.log(Level.CONFIG, login + " - ошибка изменения никнейма");
                            }

                            continue;
                        }

                        if (str.startsWith(Command.UPDATE_PASS)) {
                            String[] token = str.split(" ", 2);
                            if (server.getAuthService().updateNickname(login,token[1])) {
                                serverLogger.log(Level.FINE,login+" - успешно изменил пароль");
                                //sendMsg("Пароль успешно изменён");
                            } else {
                                sendMsg("Ошибка изменения пароля пользователя");
                                serverLogger.log(Level.CONFIG, login + " - ошибка изменения пароля пользователя");
                            }
                            continue;
                        }

                        if (str.startsWith(Command.PRIVATE))  {
                            String[] token = str.split(" ",3);
                            if (token.length<3) continue;

                            String getter = token[1];
                            String privateMsg = token[2];

                            server.privateMsg(this, getter, privateMsg);

                        } else {
                            server.broadcastMsg(this, str, false);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    sendMsg(Command.END);
                    serverLogger.log(Level.WARNING, "Timeout ожидания подключения пользователя");
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    server.unsubscribe(this);
                    serverLogger.log(Level.INFO,"Client "+login+" disconnected");
                    //System.out.println("Client disconnected");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
