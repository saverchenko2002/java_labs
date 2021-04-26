package chat.server;

import chat.database.Database;
import chat.network.IStatusCodes;
import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.StringTokenizer;

public class ChatServer implements TCPConnectionListener, IStatusCodes {

    private final Database database = new Database();

    private ChatServer() {
        System.out.println("Server running.");
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        database.addNewConnection(tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {

        if (value == null)
            return;

        else if (value.equals(DISCONNECT_TOKEN)) {
            tcpConnection.disconnect();
            return;
        }

        String[] authentication = authenticationHandler(value);
        switch (authentication[0]) {
            case (EMPTY_REQUEST):
            case (NOT_AUTHENTICATION_REQUEST):
                break;
            case (REGISTRATION_TOKEN): {
                if (database.checkLoginExistence(authentication[1]))
                    tcpConnection.sendString(EXISTING_LOGIN);
                else {
                    database.putNewUser(authentication[1], authentication[2]);
                    tcpConnection.sendString(REGISTRATION_SUCCESS_TOKEN);
                }
                break;
            }
            case (LOGIN_TOKEN): {
                if (!database.checkLoginExistence(authentication[1])) {
                    tcpConnection.sendString(NONEXISTENT_LOGIN);
                    return;
                } else if (database.checkPasswordMatch(authentication[1], authentication[2])) {
                    if (database.alreadyOnline(authentication[1])) {
                        tcpConnection.sendString(DUAL_CONNECTION_BLOCK);
                        return;
                    }
                    tcpConnection.sendString(TO_CHAT);
                    database.putUserOnlineList(authentication[1], tcpConnection);
                    sendToAllConnection(database.getLoginByConnection(tcpConnection) + " connected");
                    sendToAllConnection(ONLINE_LIST_REFRESH + " " + database.onlineList());
                } else
                    tcpConnection.sendString(WRONG_PASSWORD);
                break;
            }
        }
        String[] message = messageHandler(value);
        switch (message[0]) {
            case (EMPTY_REQUEST):
                break;
            case (SIMPLE_MESSAGE_TOKEN): {
                sendToAllConnection(database.getLoginByConnection(tcpConnection) + message[1]);
                break;
            }
            case (PRIVATE_MESSAGE_TOKEN_ACTIVATE): {
                database.getConnectionByLogin(message[1]).sendString(PRIVATE_MESSENGER_RUN + " " + message[2]);
                database.getConnectionByLogin(message[2]).sendString(PRIVATE_MESSENGER_RUN + " " + message[1]);
                break;
            }
            case (PRIVATE_MESSAGE): {
                database.getConnectionByLogin(message[1]).sendString(PRIVATE_MESSAGE + " " + message[1] + " " + message[2] + " " + message[3]);
                break;
            }
            case (PRIVATE_MESSAGE_TOKEN_DEACTIVATE): {
                database.getConnectionByLogin(message[2]).sendString(PRIVATE_MESSAGE_TOKEN_DEACTIVATE + " " + message[1] + " " + message[2] + " NaN");
                database.getConnectionByLogin(message[1]).sendString(PRIVATE_MESSAGE_TOKEN_DEACTIVATE + " " + message[2] + " " + message[1]);
                break;
            }
        }
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        database.removeConnection(tcpConnection);
        if (database.getLoginByConnection(tcpConnection) != null) {
            sendToAllConnection(database.getLoginByConnection(tcpConnection) + " disconnected");
            database.popUserOnlineList(tcpConnection);
            sendToAllConnection(ONLINE_LIST_REFRESH + " " + database.onlineList());
        }
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnection(String value) {
        for (TCPConnection connection : database.getConnections()) connection.sendString(value);
    }

    private String[] authenticationHandler(String value) {
        String[] authentication;
        StringTokenizer str;
        if (value == null) {
            authentication = new String[1];
            authentication[0] = EMPTY_REQUEST;
        } else {
            authentication = new String[3];
            str = new StringTokenizer(value, " ");
            authentication[0] = str.nextToken();
            switch (authentication[0]) {
                case (REGISTRATION_TOKEN):
                    authentication[1] = str.nextToken();
                    authentication[2] = str.nextToken();
                    break;
                case (LOGIN_TOKEN): {
                    authentication[1] = str.nextToken();
                    authentication[2] = str.nextToken();
                    break;
                }
                default: {
                    authentication[0] = NOT_AUTHENTICATION_REQUEST;
                    break;
                }
            }
        }
        return authentication;
    }

    private String[] messageHandler(String value) {
        String[] message;
        StringTokenizer str;
        if (value == null) {
            message = new String[1];
            message[0] = EMPTY_REQUEST;
        } else {
            message = new String[4];
            str = new StringTokenizer(value, " ");
            message[0] = str.nextToken();
            message[1] = "";
            switch (message[0]) {
                case (SIMPLE_MESSAGE_TOKEN):
                    while (str.hasMoreTokens()) {
                        message[1] += str.nextToken();
                        message[1] += " ";
                    }
                    break;
                case (PRIVATE_MESSAGE_TOKEN_ACTIVATE):
                case (PRIVATE_MESSAGE_TOKEN_DEACTIVATE):
                    message[1] = str.nextToken();
                    message[2] = str.nextToken();
                    break;
                case (PRIVATE_MESSAGE):
                    message[1] = str.nextToken();
                    message[2] = str.nextToken();
                    message[3] = "";
                    while (str.hasMoreTokens()) {
                        message[3] += str.nextToken();
                        message[3] += " ";
                    }
                    break;
            }
        }
        return message;
    }

    public static void main(String[] args) {
        new ChatServer();
    }
}