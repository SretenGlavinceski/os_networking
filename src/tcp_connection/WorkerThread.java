package tcp_connection;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class WorkerThread extends Thread {

    Socket socket;
    private String allDataFilePath;
    private String counterFilePath;
    private static final Semaphore
            lockAllDataFile = new Semaphore(1),
            lockCounterFile = new Semaphore(1);

    public WorkerThread(Socket socket, String allDataFilePath, String counterFilePath) {
        this.socket = socket;
        this.allDataFilePath = allDataFilePath;
        this.counterFilePath = counterFilePath;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // READ HTTP REQUEST FROM CLIENT
            WebRequest request = WebRequest.build(reader);
            System.out.printf("Client is requesting %s using %s protocol version %s\n",
                    request.url,
                    request.method,
                    request.version);

            String receivedMessage = request.headers.get("Message-Request:");
            System.out.println(receivedMessage);
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

            // RESPOND TO CLIENT
            writer.write("HTTP/1.1 OK\n");
            writer.write("Response: "+ response +"\n");
            writer.write("Content-Type: text/html\n\n");

            writer.flush();

            // SAVE ALL THE DATA FROM CLIENT WITH LOGGER SERVER

            saveClientData(request);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                assert reader != null;
                reader.close();
                assert writer != null;
                writer.close();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void saveClientData(WebRequest request) {
        RandomAccessFile writeDataFile = null;
        RandomAccessFile updateCounterFile = null;

        try {
            writeDataFile = new RandomAccessFile(this.allDataFilePath, "rw");
            updateCounterFile = new RandomAccessFile(this.counterFilePath, "rw");

            // WRITE DATA FOR CLIENT TO FILE

            lockAllDataFile.acquire();
                writeDataFile.seek(writeDataFile.length());
                writeDataFile.writeBytes(request.allData);
            lockAllDataFile.release();

            // UPDATE NUMBER OF READ LINES TO SERVER

            lockCounterFile.acquire();

                String firstLine = null;
                try {
                    firstLine = updateCounterFile.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                int currCounter = 0;
                if (firstLine != null)
                    currCounter = Integer.parseInt(firstLine);

                updateCounterFile.seek(0);
                updateCounterFile.writeBytes(String.valueOf(request.numOfLinesRead + currCounter));

            lockCounterFile.release();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                assert writeDataFile != null;
                writeDataFile.close();
                assert updateCounterFile != null;
                updateCounterFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class WebRequest {
        String method;
        String url;
        String version;
        Map<String, String> headers;
        String allData;
        int numOfLinesRead;

        private WebRequest(String method, String url, String version, Map<String, String> headers, String allData, int numOfLinesRead) {
            this.method = method;
            this.url = url;
            this.version = version;
            this.headers = headers;
            this.allData = allData;
            this.numOfLinesRead = numOfLinesRead;
        }

        public static WebRequest build(BufferedReader reader) throws IOException {
            StringBuilder sb = new StringBuilder();
            int numOfLinesRead = 1;
            String [] parts = reader.readLine().split("\\s+");

            sb.append(parts[0]).append(parts[1]).append(parts[2]).append("\n");

            Map<String, String> headers = new HashMap<>();
            String line = null;

            while (!(line = reader.readLine()).isEmpty()) {
                sb.append(line).append("\n");
                numOfLinesRead++;
                String [] mapValues = line.split("\\s+");
                System.out.println(mapValues[0] + " " + mapValues[1]);
                headers.put(mapValues[0], mapValues[1]);
            }

            return new WebRequest(parts[0], parts[1], parts[2], headers, sb.toString(), numOfLinesRead);

        }

    }

}