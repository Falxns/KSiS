package Server;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    static final int PORT = 1488;
    private static UDPMessage connectionListener;
    public static LinkedList<String> messageList = new LinkedList<>();
    public static LinkedList<String> clientsNameList = new LinkedList<>();
    public static ObservableList<ClientHandler> clients = FXCollections.observableArrayList();

    public Server() {
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            connectionListener = new UDPMessage(serverSocket.getLocalPort());
            System.out.println("Server is running.");
            while (true) {
                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                new Thread(client).start();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                System.out.println("Server stopped.");
                if (serverSocket != null) {
                    serverSocket.close();
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessageToAllClients(String msg, int from, int to, int type) {
        for (ClientHandler client : clients) {
            client.sendMsg(msg, from, to, type);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
