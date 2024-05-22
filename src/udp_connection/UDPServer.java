package udp_connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer extends Thread {
    private DatagramSocket socket;
    private int bufferSize;
    public UDPServer(int port, int bufferSize) {

        try {
            this.bufferSize = bufferSize;
            this.socket = new DatagramSocket(port);
            System.out.printf("Starting server in port %s\n", port);
        } catch (SocketException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server started...");
        }
    }

    @Override
    public void run() {
        byte [] receiveData = new byte[this.bufferSize];
        byte [] sendData = null;

        DatagramPacket receivePacket = null;
        DatagramPacket sendPacket = null;

        try {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (true) {
                socket.receive(receivePacket);
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                System.out.printf("Client on port %d sends request: %s\n", receivePacket.getPort(), receivedMessage);

                String response = null;
                switch (receivedMessage) {
                    case "login":
                        response = "logged in";
                        break;
                    case "logout":
                        response = "logged out";
                        break;
                    default:
                        response = "echo-" + receivedMessage;
                }
                sendData = response.getBytes();

                System.out.printf("Response to client with port %d is: %s\n", receivePacket.getPort(), response);

                sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                socket.send(sendPacket);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv("UDP_SERVER_PORT"));
        new UDPServer(port, 256).start();
    }

}
