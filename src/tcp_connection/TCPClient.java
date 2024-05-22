package tcp_connection;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class TCPClient extends Thread {
    private int serverPort;
    private String serverName;
    private long clientID;

    static final String [] TYPE_MESSAGES = {
            "login", "logout", "access-data", "view-images", "open-files"
    };
    static final Random random = new Random();

    public TCPClient(int serverPort, String serverName) {
        this.serverPort = serverPort;
        this.serverName = serverName;
        this.clientID = random.nextInt(100, 1000);
    }

    @Override
    public void run() {
        Socket socket = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {

            // HTTP REQUEST TO SERVER
            boolean firstTry = true;

            while(true) {
                socket = new Socket(InetAddress.getByName(this.serverName), this.serverPort);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String messageRequest = TYPE_MESSAGES[random.nextInt(5)];
                System.out.println("Client is requesting to: " + messageRequest);

                writer.write("GET / HTTP/1.1\n");
                writer.write("User-Agent: MyUserAgent\n");
                writer.write("Host: developer.mozilla.org\n");
                writer.write("Accept-Language: en\n");
                writer.write("Message-Request: "+ messageRequest +"\n");
                writer.write("ClientID: " + this.clientID + "\n\n");

                writer.flush();

                // RESPONSE FROM SERVER
                boolean logged_out = false;
                boolean logged_in = false;
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Client received -> " + line);
                    if (line.contains("Response: logged out")) {
                        logged_out = true;
                    } else if (line.contains("Response: logged in")) {
                        logged_in = true;
                    }
                }

                if (firstTry && !logged_in) {
                    System.out.println("NOT LOGGING IN ON FIRST TRY!");
                    break;
                }

                if (logged_out)
                    break;

                firstTry = false;
            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
                writer.close();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        int serverPort = Integer.parseInt(System.getenv("TCP_SERVER_PORT"));
        String serverName = System.getenv("TCP_SERVER_NAME");
        new TCPClient(serverPort, serverName).start();
    }

}
