package chat.server;

import chat.network.IStatusCodes;
import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;


public class ChatServer implements TCPConnectionListener, IStatusCodes {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private static final ConcurrentHashMap<TCPConnection, String> currentConnectionsDatabaseCS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, TCPConnection> currentConnectionsDatabaseSC = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> database = new ConcurrentHashMap<>();

    String[] authorizationInfo = new String[3];

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
        StringTokenizer str = new StringTokenizer(value, " ");
        authorizationInfo[0] = str.nextToken();
        if (authorizationInfo[0].equals(REGISTRATION_TOKEN)) {
            authorizationInfo[1] = str.nextToken();
            authorizationInfo[2] = str.nextToken();

            if (database.containsKey(authorizationInfo[1])) {
                tcpConnection.sendString(EXISTING_LOGIN);
            } else {
                database.put(authorizationInfo[1], authorizationInfo[2]);
                tcpConnection.sendString(REGISTRATION_SUCCESS_TOKEN);
            }

        } else if (authorizationInfo[0].equals(LOGIN_TOKEN)) {
            authorizationInfo[1] = str.nextToken();
            authorizationInfo[2] = str.nextToken();

            if (!database.containsKey(authorizationInfo[1])) {
                tcpConnection.sendString(NONEXISTENT_LOGIN);
            } else if (database.get(authorizationInfo[1]).equals(authorizationInfo[2])) {
                if (currentConnectionsDatabaseCS.containsValue(authorizationInfo[1])) {
                    tcpConnection.sendString(DUAL_CONNECTION_BLOCK);
                    return;
                }
                currentConnectionsDatabaseSC.put(authorizationInfo[1], tcpConnection);
                currentConnectionsDatabaseCS.put(tcpConnection, authorizationInfo[1]);
                tcpConnection.sendString(TO_CHAT);
                sendToAllConnection(currentConnectionsDatabaseCS.get(tcpConnection) + " connected");
            } else
                tcpConnection.sendString(WRONG_PASSWORD);
        } else {
            sendToAllConnection(currentConnectionsDatabaseCS.get(tcpConnection) + value);
        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        tcpConnection.disconnect();
        sendToAllConnection(currentConnectionsDatabaseCS.get(tcpConnection) + " disconnected");
        String trashCan = currentConnectionsDatabaseCS.get(tcpConnection);
        currentConnectionsDatabaseCS.remove(tcpConnection);
        currentConnectionsDatabaseSC.remove(trashCan);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnection(String value) {
        for (TCPConnection connection : connections) connection.sendString(value);
    }
}