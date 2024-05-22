package udp_connection;

import java.io.IOException;
import java.net.*;
import java.util.Random;

public class UDPClient extends Thread {

    private int serverPort;
    private InetAddress serverAddress;
    private DatagramSocket socket;
    private int bufferSize;
    static final String [] TYPE_MESSAGES = {"login", "logout", "access data", "show graph", "view images", "open files"};
    static final Random random = new Random();

    public UDPClient(int serverPort, String serverName, int bufferSize) {
        this.serverPort = serverPort;
        this.bufferSize = bufferSize;
        try {
            this.serverAddress = InetAddress.getByName(serverName);
            socket = new DatagramSocket();
        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String sendMessage = TYPE_MESSAGES[0];

        byte [] sendData = null;
        byte [] receiveData = new byte[this.bufferSize];

        DatagramPacket sendPacket = null;
        DatagramPacket receivePacket = null;


        try {
            // FIRST CLIENT REQUEST TO LOGIN
            // THIS SENDS MESSAGES UNTIL CLIENT LOG'S OUT

            while (true) {

                sendData = sendMessage.getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
                socket.send(sendPacket);
                System.out.printf("Message to server: %s\n", sendMessage);


                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.printf("Received message from server: %s\n", receivedMessage);

                if (sendMessage.equals("logout"))
                    break;

                sendMessage = TYPE_MESSAGES[random.nextInt(5) + 1];

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int udpServerPort =Integer.parseInt(System.getenv("UDP_SERVER_PORT"));
        String udpServerName = System.getenv("UDP_SERVER_NAME");
        new UDPClient(udpServerPort, udpServerName, 256).start();
    }

}
