package server;

public interface AuthService {
    /**
     * Метод получения никнейма по логину и паролю
     * @param login
     * @param password
     * @return null если учетка не найдена,
     * nickname если найдена
     */
    String getNicknameByLoginAndPassword(String login, String password);


    /**
     * метод для регистрации учётной записи
     * @param login
     * @param password
     * @param nickname
     * @return true при успешной регистрации
     * false если логин или никнейм уже заняты, или регистрация не получилась
     */
    boolean registration(String login, String password, String nickname);
}
