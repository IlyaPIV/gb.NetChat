package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public PasswordField passField;
    @FXML
    public TextField loginField;
    @FXML
    public HBox AuthPanel;
    @FXML
    public HBox MsgPanel;

    private Stage stage;

    private Socket socket;
    private static final int PORT = 8189;
    private static final String ADDRESS = "localhost";

    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickname;


    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;

        AuthPanel.setVisible(!authenticated);
        AuthPanel.setManaged(!authenticated);
        MsgPanel.setVisible(authenticated);
        MsgPanel.setManaged(authenticated);

        if (!authenticated){
            nickname="";
        }

        textArea.clear();
        setTitle(nickname);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Platform.runLater(()->{
            stage = (Stage) textArea.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                //System.out.println("bye");
                if (socket!=null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);

    }

    private void connect(){
        try {
            socket = new Socket(ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                try {
                    //authentication
                    while (true){
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/auth_OK")){
                                nickname = str.split(" ")[1];
                                setAuthenticated(true);
                                break;
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }

                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            break;
                        }

                        textArea.appendText(str+"\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) {

        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        String msg = String.format("/auth %s %s", loginField.getText().trim(), passField.getText().trim());
        passField.clear();

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname){
        String title;
        if (nickname.equals("")) {
            title = "Let's chat a bit!";
        } else {
            title = String.format("[%s], Let's chat!!!", nickname);
        }
        Platform.runLater(()->{
            stage.setTitle(title);
        } );
    }
}