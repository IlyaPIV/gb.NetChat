package server;

import java.sql.*;
import org.apache.commons.lang3.*;


public class SqliteAuthService implements AuthService{

    private static Connection connection;
    private static Statement stmt;

    public SqliteAuthService(){
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:Server/serverChat.db");
        stmt = connection.createStatement();
  //     prepareSQL();
    }

    public void disconnect() {
        try {
            if (connection!=null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            if (incorrectValue(login)) return null; //некорректное содержимое переменной
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM users WHERE login=\"%s\";",login));
            if (rs.isClosed()) {
              //  System.out.println("нет записей");
                return null;
            }
            while (rs.next()) {
                if (password.equals(rs.getString(3))) {
                //    System.out.println("пароли совпали");
                    return rs.getString(4);
                }
            }
            //System.out.println("пароли не равны");
            return null;
        } catch (Exception e ) {
            //System.out.println("ошибка при поиске юзера");
            return null;
        }
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            if (incorrectValue(login) || incorrectValue(password) || incorrectValue(nickname)) return false;
            if (findUser(login)) return false;
            addNewUser(login, password, nickname);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateNickname(String login, String nickname){
        if (incorrectValue(login) || incorrectValue(nickname)) return false;
        return updateUser(login, "", nickname);
    }

    @Override
    public boolean updatePassword(String login, String pass){
        if (incorrectValue(login) || incorrectValue(pass)) return false;
        return updateUser(login, pass, "");
    }

    private void prepareSQL(){
        try {
            stmt.executeUpdate("DELETE FROM users;");
            addNewUser("Vasia","123","Vasia-Nagibator");
            addNewUser("qwerty","qwerty", "qwerty");
            addNewUser("abc","abc","abc");
            addNewUser("zxc","zxc","zxc");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean incorrectValue(String value) {
       // return value.contains("TABLE");
        return StringUtils.containsIgnoreCase(value," table ");
    }

    private void addNewUser(String login, String pass, String nick) throws SQLException{
        stmt.executeUpdate(String.format("INSERT INTO users (login, password, nickname) VALUES (\"%s\", \"%s\", \"%s\");", login,pass,nick));
    }

    private boolean findUser(String login) throws SQLException {
        ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM users WHERE login=\"%s\";",login));
        return (!rs.isClosed());
    }

    private boolean updateUser(String login, String pass, String nick){
        try {
            if (pass.equals("")) stmt.executeUpdate(String.format("UPDATE users SET nickname = \"%s\" WHERE login=\"%s\";", login, nick));
                else stmt.executeUpdate(String.format("UPDATE users SET password = \"%s\" WHERE login=\"%s\";", login, pass));
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


}
