package client;

import constants.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passField;
    @FXML
    public TextField nicknameField;
    @FXML
    public TextArea systemTextArea;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }


    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passField.getText().trim();
        String nickname = nicknameField.getText().trim();

        controller.registration(login,password,nickname);
    }

    public void regResult(String command) {
        if (command.equals(Command.REG_OK)) {
            systemTextArea.appendText("Регистрация прошла успешно");
        } else {
            systemTextArea.appendText("Ошибка регистрации пользователя: логин/никнейм заняты");
        }
    }
}
