package chat.network;

public class ConnectionInfo {
    private final String login;
    private final String password;

    public ConnectionInfo(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
