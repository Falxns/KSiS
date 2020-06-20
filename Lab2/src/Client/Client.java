package Client;

import Server.Message;
import Server.Server;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Client {

    private Socket clientSocket;
    public static ObjectInputStream inMessage;
    private BufferedReader bufferedReader;
    public static ObjectOutputStream outMessage;
    private static boolean isExit = false;
    public static boolean isClose = false;
    public static String clientName;
    private Chat chat;

    public void clientWork() throws Exception{
        try {
            while (true) {
                if (isExit || isClose)
                    break;
                if (bufferedReader.ready()) {
                    Message answer = (Message) inMessage.readObject();
                    System.out.println(answer.msg);
                    switch (answer.type) {
                        case 0:
                            if (answer.to != 0) {
                                chat.addMsg(answer.msg, answer.from);
                            } else {
                                chat.addMsg(answer.msg, 0);
                            }
                            break;
                        case 1:
                            chat.addChat(answer.msg);
                            break;
                        case 2:
                            chat.deleteChat(answer.from);
                            break;
                        default:
                            System.err.println("Unknown command.");
                    }
                }
            }
        } catch (IOException ex) {
            close();
        }
    }

    public Client(String name){
        clientName = name;
        try {
            byte[] buf = new byte[4];
            if (!isExit) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.100.255"), 7777);
                DatagramSocket datagramSocket = new DatagramSocket();
                datagramSocket.send(datagramPacket);
                datagramSocket.receive(datagramPacket);
                datagramSocket.close();

                InetAddress address = datagramPacket.getAddress();
                ByteBuffer byteBuffer = ByteBuffer.wrap(datagramPacket.getData());
                int port = byteBuffer.getInt();

                clientSocket = new Socket(address, port);

                inMessage = new ObjectInputStream(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outMessage = new ObjectOutputStream(clientSocket.getOutputStream());

            }
        } catch (IOException e) {
            System.out.println("Can't connect to server, check network connection.");
            close();
        }

        chat = new Chat(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientWork();
                } catch (Exception e) {
                    System.out.println("Client lost the connection.");
                    close();
                }
            }
        }).start();

        try {
            Message message = new Message(clientName, 0, 0, 1);
            outMessage.writeObject(message);
            outMessage.flush();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void sendMsg(String msg, int from, int to, int type) throws IOException {
        Date time;
        String dtime;
        SimpleDateFormat dt1;
        try {
            time = new Date();
            dt1 = new SimpleDateFormat("HH:mm:ss");
            dtime = dt1.format(time);
            Message message = new Message("(" + dtime + ")" + clientName + ": " + msg, from, to, type);
            outMessage.writeObject(message);
            outMessage.flush();
        } catch (IOException e) {
            close();
        }
    }

    public void close() {
        try {
            if (!clientSocket.isClosed()) {
                System.out.println("Exit");
                isClose = true;
                sendMsg("", 0, 0, 2);
                clientSocket.close();
                inMessage.close();
                outMessage.close();
            }
        } catch (IOException ignored) {}
    }
}