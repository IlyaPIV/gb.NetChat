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
}
