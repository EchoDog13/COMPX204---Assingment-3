import java.net.*;
import java.io.*;

public class TftpClient {
    private String serverHostname;
    private Integer serverPort = 20202;
    private Integer clientPort;;
    private static String filePath;
    private InetAddress address;

    private DatagramSocket ds = null;

    /**
     * @param args
     */
    public static void main(String args[]) {
        // Initial port number for server

        // Read in argument (port number excluded)
        // Args: hostName, port, filePath
        TftpClient client = TftpClientManager.client;
        client.setAddress();
        client.processUserRequest(args);

        client.sendRQQ();
        client.recieveFile();

        // Send RRQ Packet to Server to request file
        try {
            // DatagramSocket ds = new DatagramSocket();

            // InetAddress addr = InetAddress.getByName(serverHostname);

            // byte[] buf = new byte[1472];
            // buf = ("1 " + filePath).getBytes();
            // DatagramPacket p = new DatagramPacket(buf, buf.length, addr, serverPort);
            // // Sends packet to server

            // ds.send(p);

            // System.out.println(new String(p.getData(), 0, p.getLength()));

        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public class TftpClientManager {
        public static TftpClient client;

        static {
            client = new TftpClient();
        }
    }

    public void sendRQQ() {
        try {

            TftpClient client = TftpClientManager.client;

            byte[] buf = new byte[1472];
            buf = ("1 " + filePath).getBytes();

            // Sends packet to server
            client.sendResponse(buf);

        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public void processUserRequest(String args[]) {
        if (args.length >= 2) {
            setHostName(args[0]);
            setPort(Integer.parseInt(args[1]));
            setFilePath(args[2]);
        }
    }

    public void sendResponse(byte[] buffer) {
        // Send the response to the client
        DatagramPacket response = new DatagramPacket(buffer, buffer.length, getAddress(), getPort());
        System.out.println(
                "Sending response to " + getHostName() + " on port " + response.getPort());
        try {
            socket().send(response);
            System.out.println("request sent");
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

    }

    public DatagramSocket socket() {

        if (ds == null) {
            try {
                ds = new DatagramSocket();
            } catch (Exception e) {
                System.err.println("Exception: " + e);
            }
        }
        return ds;
    }

    public void recieveFile() {

        try {

            byte[] recieveBuffer = new byte[514];
            DatagramPacket p;
            // TURN BACK ON TO SET TO FILE PATH
            // File recievedFile = new File(getFilePath());

            File recievedFile = new File("recievedFile.txt");

            recievedFile.createNewFile();
            TftpClient client = TftpClientManager.client;

            do {

                // recieveBuffer
                // Create a new file output stream matching file name
                FileOutputStream fos = new FileOutputStream(recievedFile, true);

                p = new DatagramPacket(recieveBuffer, recieveBuffer.length);
                client.ds.receive(p);

                byte[] data = new byte[p.getLength()];
                System.arraycopy(p.getData(), 0, data, 0, p.getLength());
                // byte[] data = p.getData().toString().getBytes();

                System.out.println("Received request from " + p.getAddress() + " on port " + p.getPort());
                // Prints out the request to the console
                String receivedRequest = new String(data, 0, p.getLength(), "UTF-8");

                System.out.println("Request:" + receivedRequest);

                if (receivedRequest.startsWith("1")) {
                    System.out.println("Received RRQ Packet");

                } else if (receivedRequest.startsWith("2")) {
                    System.out.println("Received Data Packet");

                    char packetNum = receivedRequest.charAt(1);
                    int packetNumInt = Character.getNumericValue(packetNum);
                    // Remove header from packet
                    // recieveBuffer = new byte[p.getLength() - 2];

                    fos.write(recieveBuffer, 2, p.getLength() - 2);

                    ackPacket(packetNumInt);

                } else if (receivedRequest.startsWith("4")) {
                    System.out.println("Error Packet: File not found");

                    return;
                }

            } while (p.getLength() != 0 || p.getLength() == 514);

            // TftpServerWorker worker = new TftpServerWorker(p);
            // worker.start();
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

        return;
    }

    public void ackPacket(int packetNum) {
        try {
            TftpClient client = TftpClientManager.client;

            byte[] buf = new byte[1472];
            buf = ("3" + packetNum).getBytes();

            // Sends packet to server
            System.out.println();
            client.sendResponse(buf);

        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

    }

    public int getPort() {
        return serverPort;
    }

    public void setPort(int port) {
        this.serverPort = port;
        System.out.println("Port set to: " + port);
    }

    public String getHostName() {
        return serverHostname;
    }

    public void setHostName(String hostName) {
        this.serverHostname = hostName;
        System.out.println("Host Name set to: " + hostName);
    }

    public InetAddress getAddress() {
        return address;

    }

    public void setAddress() {
        try {
            TftpClient client = TftpClientManager.client;
            // Resolving the hostname to an IP address
            this.address = InetAddress.getByName(client.getHostName());

        } catch (UnknownHostException e) {
            System.err.println("Failed to resolve host: " + e.getMessage());
        }
    }

    public static String getFilePath() {
        return filePath;
    }

    public static void setFilePath(String filePath) {
        TftpClient.filePath = filePath;
        System.out.println("File Path set to: " + filePath);
    }
}
