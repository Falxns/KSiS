package Client;

import Server.Message;
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
import java.util.List;

public class Client {

    private Socket clientSocket;
    public static ObjectInputStream inMessage;
    private BufferedReader bufferedReader;
    public static ObjectOutputStream outMessage;
    private static boolean isExit = false;
    public static boolean isClose = false;
    public static String clientName;

    @FXML
    private TextField login;

    @FXML
    public void logIn() throws IOException {
        clientName = login.getText();
        sendMsg();
    }

    public void startGame() throws Exception{
        Stage tempStage = (Stage) login.getScene().getWindow();
        tempStage.hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Chat.fxml"));
        Parent root = (Parent) loader.load();
        //controller = loader.getController();
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Chat");
        Scene scene = new Scene(root, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(windowEvent -> {
            isClose = true;
            primaryStage.close();
        });
        //controller.initialize();
        primaryStage.show();
    }

    public void clientWork() throws Exception{
        while (true) {
            if (isExit)
                break;
            if (bufferedReader.ready()) {
                Message answer = (Message) inMessage.readObject();
                System.out.println(answer.msg);
//                if (answer.msg.equals("start")) {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                startGame();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                }
//                if (answer.msg.equals("lobbies")) {
//
//                }
            }
            if (isClose){
                sendMsg(,,,);
                outMessage.close();
                inMessage.close();
                clientSocket.close();
                isExit = true;
                break;
            }
        }
    }

    public Client(){
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
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientWork();
                } catch (Exception e) {
                    System.out.println("Client lost the connection.");
                }
            }
        }).start();

    }

    private void sendMsg(String msg, int from, int to, int type) throws IOException {
        Message message = new Message(msg, from, to, type);
        outMessage.writeObject(message);
        outMessage.flush();
    }
}