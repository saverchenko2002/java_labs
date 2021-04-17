package chat.server;

import chat.network.ConnectionInfo;
import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private static final HashMap<TCPConnection, String> database = new HashMap<>();


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
        connections.add(tcpConnection);
    }


    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        StringTokenizer str = new StringTokenizer(value, " ");
        if (str.nextToken().equals("false")) {
            database.put(tcpConnection, str.nextToken());
            sendToAllConnection("Client connected: " + database.get(tcpConnection));

        } else
            sendToAllConnection(database.get(tcpConnection) + value);
    }


    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnection("Client disconnected: " + database.get(tcpConnection));
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnection(String value) {
        System.out.println(value);
        for (TCPConnection connection : connections) connection.sendString(value);
    }
}
