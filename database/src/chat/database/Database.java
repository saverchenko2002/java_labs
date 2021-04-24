package chat.database;

import chat.network.TCPConnection;

import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private static final ConcurrentHashMap<String, String> database = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<TCPConnection, String> currentUsersConnectionLogin = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, TCPConnection> currentUsersLoginConnection = new ConcurrentHashMap<>();

    public Database() {

    }

    public void putNewUser(String login, String password) {
        database.put(login,password);
    }

    public boolean checkLoginExistence(String login) {
        return database.containsKey(login);
    }

    public boolean checkPasswordMatch(String login, String password) {
        return (database.get(login).equals(password));
    }

    public void putUserOnlineList(String login, TCPConnection tcpConnection) {
        currentUsersConnectionLogin.put(tcpConnection, login);
        currentUsersLoginConnection.put(login, tcpConnection);
    }

    public boolean alreadyOnline(String login) {
        return currentUsersConnectionLogin.containsValue(login);
    }

    public String getLoginByConnection(TCPConnection tcpConnection) {
        return currentUsersConnectionLogin.get(tcpConnection);
    }
}
