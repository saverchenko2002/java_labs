package chat.server;

import chat.database.Database;
import chat.network.IStatusCodes;
import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;


public class ChatServer implements TCPConnectionListener, IStatusCodes {


    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    Database database = new Database();


//    String clientsOnline;


    private ChatServer() {
        System.out.println("Server running");
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
        connections.add(tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
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
            case (PRIVATE_MESSAGE_TOKEN): {
                break;
            }
        }
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
//        sendToAllConnection(currentConnectionsDatabaseCS.get(tcpConnection) + " disconnected");
//        String trashCan = currentConnectionsDatabaseCS.get(tcpConnection);
//        currentConnectionsDatabaseCS.remove(tcpConnection);
//        currentConnectionsDatabaseSC.remove(trashCan);
//        clientsOnline = ONLINE_LIST_REFRESH + " ";
//        clientsOnline += currentConnectionsDatabaseSC.keySet().toString();
//        sendToAllConnection(clientsOnline);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnection(String value) {
        for (TCPConnection connection : connections) connection.sendString(value);
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
            message = new String[2];
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
                case (PRIVATE_MESSAGE_TOKEN):
                    break;
            }
        }
        return message;
    }


    public static void main(String[] args) {
        new ChatServer();
    }
}