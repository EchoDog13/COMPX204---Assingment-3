import java.net.*;
import java.util.List;
import java.io.*;
import java.lang.reflect.Array;

public class TftpClient {
    private static String serverHostname;
    private static Integer serverPort = 69;
    private static Integer clientPort;
    private static String filePath;
    private static InetAddress address;

    private static DatagramSocket ds = null;

    /**
     * @param args
     */
    public static void main(String args[]) {

        // Read in argument
        // Args: hostName, port, filePath

        filePath = args[2];
        serverPort = Integer.parseInt(args[1]);
        serverHostname = args[0];

        try {
            // Get the address of the server from the parsed host hame
            address = InetAddress.getByName(serverHostname);
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
        // Send the request for the file to the server
        sendRQQ();
        // Recieve the file from the server
        recieveFile();

        // Send RRQ Packet to Server to request file
        try {

        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    /**
     * 
     */
    public static void sendRQQ() {
        try {

            byte[] buf = new byte[filePath.getBytes().length + 1];
            buf[0] = 1;
            byte[] filePathBytes = filePath.getBytes();
            System.arraycopy(filePathBytes, 0, buf, 1, filePathBytes.length);

            // Sends packet to server
            sendResponse(buf);

        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public static void processUserRequest(String args[]) {
        if (args.length >= 2) {
            serverHostname = args[0];
            serverPort = Integer.parseInt(args[1]);
            filePath = args[2];
        }
    }

    /**
     * Used to centralize the sending of responses to the client
     * 
     * @param buffer the buffer to send to the client
     */
    public static void sendResponse(byte[] buffer) {
        // Send the response to the client
        DatagramPacket response = new DatagramPacket(buffer, buffer.length, address, serverPort);
        System.out.println(
                "Sending response to " + serverHostname + " on port " + response.getPort());
        try {
            socket().send(response);
            String tempString = new String(buffer, "UTF-8");
            System.out.println("Client Request" + tempString);
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

    }

    /**
     * Creates a socket for the server
     * 
     * @return the socket
     */
    public static DatagramSocket socket() {
        // If the socket is null, create a new socket
        if (ds == null) {
            try {
                ds = new DatagramSocket();
            } catch (Exception e) {
                System.err.println("Exception: " + e);
            }
        }
        // Return the socket
        return ds;
    }

    /**
     * Recieves a file from the server
     */
    public static void recieveFile() {

        try {
            byte[] recieveBuffer = new byte[514];
            byte[] blockDataBuffer;
            byte blockNumber = 0;
            byte previouseBlockNumber = -1;
            DatagramPacket p;
            // TURN BACK ON TO SET TO FILE PATH
            File recievedFile = new File("received_" + filePath);

            // Loop while packets == 514
            do {
                // recieveBuffer
                // Create a new file output stream matching file name
                p = null;
                recieveBuffer = new byte[1472];
                p = new DatagramPacket(recieveBuffer, 1472);
                ds.receive(p);

                address = p.getAddress();
                serverPort = p.getPort();
                byte[] data = p.getData();
                // byte[] data = p.getData().toString().getBytes();

                blockDataBuffer = new byte[p.getLength() - 2];
                System.arraycopy(data, 2, blockDataBuffer, 0, p.getLength() - 2);
                blockNumber = data[1];
                byte packetType = data[0];

                if (blockNumber != previouseBlockNumber) {
                    System.out.println("Block Number: " + blockNumber);
                    writeToFile(blockDataBuffer, recievedFile);
                    previouseBlockNumber = blockNumber;
                    ackPacket(blockNumber);
                } else {
                    System.out.println("Duplicate Packet");
                    ackPacket(blockNumber);
                    continue;
                }

                System.out.println("Received request from " + p.getAddress() + " on port " + p.getPort());
                // Prints out the request to the console

                System.out.println("Request: " + new String(blockDataBuffer));

                if (packetType == 4) {
                    System.out.println("Error Packet: File not found");

                    return;
                }

            } while (p.getLength() == 514);

        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
        return;
    }

    /**
     * Writes data to a file - called once file block is recieved and confirmd
     * 
     * @param data Data to write to file
     * @param file File to write to
     */
    public static void writeToFile(byte[] data, File file) {
        try {
            // Create a new file output stream matching file name
            FileOutputStream fos = new FileOutputStream(file, true);
            // Write the data to the file
            fos.write(data);
            // Close the file output stream
            fos.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public static void ackPacket(int packetNum) {
        try {

            // Create a new buffer used to send the ACK response
            byte[] buf = new byte[2];

            // Set the first byte to 3 as it is an ACK
            buf[0] = 3;

            // Set the second byte to the packet number
            buf[1] = (byte) packetNum;

            // Sends ack packet to server
            sendResponse(buf);

        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

    }

}
