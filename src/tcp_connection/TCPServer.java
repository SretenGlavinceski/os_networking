package tcp_connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {

    private int port;

    public TCPServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("Starting server...");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Server started");
        }

        while (true) {
            try {
                Socket client = serverSocket.accept();
                System.out.println("new client - creating new worker thread...");
                new WorkerThread(
                        client,
                        "/usr/src/myapp/data/csvFile.txt",
                        "/usr/src/myapp/data/counterFile.txt").start();

                System.out.println(client.getInetAddress() + " " + client.getPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv("TCP_SERVER_PORT"));
        TCPServer server = new TCPServer(port);
        server.start();
    }

}
