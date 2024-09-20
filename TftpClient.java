import java.net.*;
import java.util.List;
import java.io.*;
import java.lang.reflect.Array;

/**
 * TFTP Client
 * * ID: 1630724
 * 
 */
public class TftpClient {
    // Store the server hostname;
    private static String serverHostname;
    // Set the default port to 69 for the server
    private static Integer serverPort = 69;
    // private static Integer clientPort;
    private static String filePath;
    // Create a new address;
    private static InetAddress address;

    // Create a new socket null to be initialized later
    private static DatagramSocket ds = null;

    /**
     * @param args
     */
    public static void main(String args[]) {

        // Read in args from the command line
        // Args: hostName, port, filePath

        // Set the server hostname, port, and file path
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
     * Sends a RRQ packet to the server
     */
    public static void sendRQQ() {
        try {
            // Create a new buffer used to send the RRQ request
            byte[] buf = new byte[filePath.getBytes().length + 1];
            // Set the first byte to 1 as it is a RRQ
            buf[0] = 1;
            // Create Byte array of the file path
            byte[] filePathBytes = filePath.getBytes();
            // Copy the file path bytes to the buffer
            System.arraycopy(filePathBytes, 0, buf, 1, filePathBytes.length);

            // Sends packet to server
            sendResponse(buf);

        } catch (Exception e) {
            System.err.println("Exception: " + e);
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
        // Print out the response to the console
        System.out.println("Sending response/request to " + serverHostname + " on port " + response.getPort());
        try {
            // Send the response to the server
            socket().send(response);
            // Print out the request to the console
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
                // Clear datagram packet from any previous data
                p = null;
                recieveBuffer = new byte[1472];
                p = new DatagramPacket(recieveBuffer, 1472);
                ds.receive(p);

                // Get and set the address and port of the server
                address = p.getAddress();
                serverPort = p.getPort();
                byte[] data = p.getData();

                // Create a new buffer to store the data from the packet without the header
                blockDataBuffer = new byte[p.getLength() - 2];
                // Copy the data from the packet to the buffer
                System.arraycopy(data, 2, blockDataBuffer, 0, p.getLength() - 2);
                // Get the block number from the packet
                blockNumber = data[1];
                // Get the packet type from the packet
                byte packetType = data[0];

                // Check if the block number is the same as the previous block number to
                // determine if the packet is the next packet
                if (blockNumber != previouseBlockNumber) {
                    System.out.println("Block Number: " + blockNumber);
                    // Write the data to the file
                    writeToFile(blockDataBuffer, recievedFile);
                    // Increment the block number
                    previouseBlockNumber = blockNumber;
                    // Send an ACK packet to the server
                    ackPacket(blockNumber);
                } else {
                    // If the block number is the same as the previous block number, the packet is a
                    // duplicate
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
                // Size of a full packet
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

    /**
     * Sends an ACK packet to the server
     * 
     * @param packetNum the packet number to send the ACK for
     */
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
