package chat.server;

import chat.network.ConnectionInfo;
import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private  final ArrayList<TCPConnection> connections = new ArrayList<>();
    private static final HashMap<TCPConnection, ConnectionInfo> database = new HashMap<>();

    private ChatServer() {
        System.out.println("Server running.");
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try {
                    System.out.println(connections.size());
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
        System.out.println(ChatServer.getDatabase().get(tcpConnection).getLogin());
        sendToAllConnection("Client connected: " + ChatServer.getDatabase().get(tcpConnection).getLogin());
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnection(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnection("Client disconnected: " + ChatServer.getDatabase().get(tcpConnection).getLogin());
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    private void sendToAllConnection(String value) {
        System.out.println(value);
        for (TCPConnection connection : connections) connection.sendString(value);
    }

    public static HashMap<TCPConnection, ConnectionInfo> getDatabase() {
        return database;
    }


}
