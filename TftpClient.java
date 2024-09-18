import java.net.*;
import java.util.List;
import java.io.*;
import java.lang.reflect.Array;

public class TftpClient {
    private String serverHostname;
    private Integer serverPort = 20202;
    private Integer clientPort;
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
        filePath = args[2];
        client.setAddress();
        client.processUserRequest(args);

        client.sendRQQ();
        client.recieveFile();

        // Send RRQ Packet to Server to request file
        try {

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
            String tempString = new String(buffer, "UTF-8");
            System.out.println("Client Request" + tempString);
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
            byte[] blockDataBuffer;
            byte blockNumber = 0;
            byte previouseBlockNumber = -1;
            DatagramPacket p;
            // TURN BACK ON TO SET TO FILE PATH
            File recievedFile = new File(getFilePath());

            // File recievedFile = new File();

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

                blockDataBuffer = new byte[p.getData().length - 2];
                System.arraycopy(p.getData(), 2, blockDataBuffer, 0, p.getData().length - 2);
                blockNumber = (byte) ((char) p.getData()[1]);
                byte packetType = p.getData()[0];

                if (blockNumber != previouseBlockNumber) {
                    System.out.println("Block Number: " + blockNumber);
                    writeToFile(blockDataBuffer, recievedFile);
                    previouseBlockNumber = blockNumber;
                } else {
                    System.out.println("Duplicate Packet");
                    ackPacket(blockNumber);
                    continue;
                }

                System.out.println("Received request from " + p.getAddress() + " on port " + p.getPort());
                // Prints out the request to the console
                String receivedRequest = new String(data, 0, p.getLength(), "UTF-8");

                System.out.println("Request:" + receivedRequest);

                if (packetType == 2) {
                    System.out.println("Received Data Packet: " + blockNumber);

                    fos.write(recieveBuffer, 2, p.getLength() - 2);

                    ackPacket(blockNumber);

                } else if (packetType == 4) {
                    System.out.println("Error Packet: File not found");

                    return;
                }

            } while (p.getLength() == 514);

            // TftpServerWorker worker = new TftpServerWorker(p);
            // worker.start();
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

        return;
    }

    public void writeToFile(byte[] data, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public void ackPacket(int packetNum) {
        try {
            TftpClient client = TftpClientManager.client;

            byte[] buf = new byte[2];
            // buf[0] = 3;
            // buf[1] = (byte) packetNum;
            buf = ("3" + packetNum).getBytes();

            // write to buf

            // Sends packet to server

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
        return "recived_" + filePath;
    }

    public static void setFilePath(String filePath) {
        TftpClient.filePath = filePath;
        System.out.println("File Path set to: " + filePath);
    }
}
