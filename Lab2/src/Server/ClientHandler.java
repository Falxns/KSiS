package Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread{
    private Socket clientSocket = null;
    private ObjectOutputStream outMessage;
    private BufferedReader bufferedReader;
    private ObjectInputStream inMessage;
    private boolean isClose = false;

    public ClientHandler(Socket socket) {
        try {
            this.clientSocket = socket;
            this.outMessage = new ObjectOutputStream(socket.getOutputStream());
            this.inMessage = new ObjectInputStream(socket.getInputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            for (String msg : Server.messageList) {
                sendMsg(msg,0,0,0,Server.filesInMsgList.get(Server.messageList.indexOf(msg)));
            }
            for (String member : Server.clientsNameList) {
                sendMsg(member,0,0,1,"");
            }
            start();
        } catch (IOException ex) {
            System.out.println("Couldn't connect to server.");
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (!isClose && inMessage != null && outMessage != null && !clientSocket.isClosed()) {
                    Message clientMessage = (Message) inMessage.readObject();
                    clientMessage.from = Server.clients.indexOf(this) + 1;
                    switch (clientMessage.type) {
                        case 0:
                            if (clientMessage.to == 0) {
                                Server.messageList.add(clientMessage.msg);
                                Server.filesInMsgList.add(clientMessage.files);
                                sendMessageToAllClients(clientMessage.msg,clientMessage.from,0,0, clientMessage.files);
                            } else {
                                Server.clients.get(clientMessage.to - 1).sendMsg(clientMessage.msg,clientMessage.from,clientMessage.to,0, clientMessage.files);
                                if (clientMessage.to != clientMessage.from) {
                                    Server.clients.get(clientMessage.from - 1).sendMsg(clientMessage.msg,clientMessage.to,clientMessage.from,0, clientMessage.files);
                                }
                            }
                            break;
                        case 1:
                            Server.clientsNameList.add(clientMessage.msg);
                            sendMessageToAllClients(clientMessage.msg,0,0,1,"");
                            break;
                        case 2:
                            sendMessageToAllClients("",clientMessage.from,0,2,"");
                            this.close();
                            break;
                        default:
                            System.out.println("Unknown command.");
                    }
                }
                sleep(100);
            }
        }
        catch (InterruptedException | IOException | NullPointerException | ClassNotFoundException ex) {
            System.out.println("Client lost connection with server.");
            close();
        }
    }

    public void sendMsg(String msg, int from, int to, int type, String files) {
        try {
            if (!clientSocket.isClosed()) {
                Message message = new Message(msg, from, to, type, files);
                outMessage.writeObject(message);
                outMessage.flush();
            }
        } catch (Exception ex) {
            System.out.println("Client lost connection with server.");
        }
    }

    public void sendMessageToAllClients(String msg, int from, int to, int type, String files) {
        for (ClientHandler client : Server.clients) {
            client.sendMsg(msg, from, to, type, files);
        }
    }

    public void close() {
        try {
            if (!clientSocket.isClosed()) {
                System.out.println("Exit");
                isClose = true;
                clientSocket.close();
                inMessage.close();
                outMessage.close();
                this.interrupt();
                int index = Server.clients.indexOf(this);
                Server.clients.remove(index);
                Server.clientsNameList.remove(index);
            }
        } catch (IOException ignored) {}
    }
}