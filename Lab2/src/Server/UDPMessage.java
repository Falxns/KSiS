package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPMessage extends Thread {
    private DatagramSocket datagramSocket;
    private byte[] buf = new byte[4];
    private int serverPort;
    private boolean isRunning = true;

    public UDPMessage(int port) {
        serverPort = port;
        start();
    }

    public void downService() {
        datagramSocket.close();
    }

    public void run()
    {
        try
        {
            datagramSocket = new DatagramSocket(7777);
            System.out.println("UDP server is running.");
            while(isRunning)
            {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    datagramSocket.receive(packet);

                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    buf[0] = (byte)((serverPort >> 24) & 0xff);
                    buf[1] = (byte)((serverPort >> 16) & 0xff);
                    buf[2] = (byte)((serverPort >> 8) & 0xff);
                    buf[3] = (byte)(serverPort & 0xff);
                    System.out.println("Server sends " + address.toString() + ":" + port + " port " + serverPort);
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    for (int i = 0; i < 1000;i++) {
                        datagramSocket.send(packet);
                    }
                } catch (IOException ex) {
                    System.out.println("Server UDP socket send/receive error.");
                    isRunning = false;
                }
            }
        }
        catch(SocketException se)
        {
            System.out.println("UDP server didn't open.");
        }
        finally {
            downService();
        }
    }
}