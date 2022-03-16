package client;




import constants.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    @FXML
    public ListView<String> clientList;

    private Stage stage;
    private Stage regStage;
    private RegController regController;

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
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

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
                        out.writeUTF(Command.END);
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
                            if (str.equals(Command.END)) {
                                break;
                            }
                            if (str.startsWith(Command.AUTH_OK)){
                                nickname = str.split(" ")[1];
                                setAuthenticated(true);
                                break;
                            }
                            if (str.equals(Command.REG_OK) || str.equals(Command.REG_FAIL)) {
                                regController.regResult(str);
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }

                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();

                    if (str.startsWith("/")) {
                        if (str.equals(Command.END)) {
                            break;
                        }

                        if (str.startsWith(Command.CLIENT_LIST)) {
                            String[] token = str.split(" ");

                            Platform.runLater(()->{
                                clientList.getItems().clear();
                                for (int i=1; i<token.length; i++)
                                {
                                    clientList.getItems().add(token[i]);
                                }
                            });
                        }

                        if (str.startsWith(Command.UPDATE_OK)) {
                            nickname = str.split(" ")[1];
                            setTitle(nickname);
                        }
                    } else {
                            textArea.appendText(str + "\n");
                        }
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

        String msg = String.format("%s %s %s", Command.AUTH, loginField.getText().trim(), passField.getText().trim());
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

    @FXML
    public void clientListMouseAction(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount()==2) {
            textField.clear();
            textField.appendText("/w "+clientList.getSelectionModel().getSelectedItem()+" ");
            textField.requestFocus();
        }
    }

    private void createRegStage(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/reg.fxml"));

        try {
            Parent root = fxmlLoader.load();

            regStage = new Stage();

            regStage.setTitle("Registration");
            regStage.setScene(new Scene(root, 300, 400));

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void tryToReg(ActionEvent actionEvent) {
        if (regStage==null) {
            createRegStage();
        }

        regStage.show();
    }

    public void registration(String login, String password, String nickname){
        String msg = String.format("%s %s %s %s", Command.REG, login, password, nickname);

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}