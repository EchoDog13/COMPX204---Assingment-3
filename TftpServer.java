import java.net.*;
import java.nio.Buffer;
import java.io.*;
import java.util.*;

/**
 * TFTP Server using TFTP protocol to send files to the client
 */
class TftpServerWorker extends Thread {
    private DatagramPacket req;
    private static final byte RRQ = 1;
    private static final byte DATA = 2;
    private static final byte ACK = 3;
    private static final byte ERROR = 4;
    private byte recievedBlockNumber = 0;

    private DatagramSocket ds = null;

    /**
     * Sends a file to the receiver.
     * 
     * @param filename the name of the file to be sent
     */
    private void sendfile(String filename) {

        try {
            // Creates file to be send based on request file name
            File file = new File(filename);
            // Check if file exists
            if (!file.exists()) {
                System.out.println("File does not exist");
                // Send Error response
                sendResponse(new byte[] { 4, "File does not exist".getBytes()[1] });
                // Send error message
                return;
            }

            // Gets the blocks of the file to be send
            List<byte[]> blocks = getBlocks(file);
            // Block number tracks the block number of the file
            byte blockNumber = 0;
            // Last block is a boolean that checks if the block is the last block
            boolean lastBlock = false;
            // number of times the ack has failed
            int ackFailures = 0;

            // Iterates through the blocks of the file from the blocks byte array
            for (int i = 0; i < blocks.size(); i++) {
                ackFailures = 0;
                byte[] currentBlock = blocks.get(i);

                // Check if this is the last block as it will be less than 512 bytes
                lastBlock = currentBlock.length < 512;

                // Loop until the ACK is received
                boolean ackReceived = false;
                while (!ackReceived) {
                    // Create the packet to send the current block
                    DatagramPacket packet = makePacket(DATA, blockNumber, currentBlock);
                    // Send the packet via the DatagramSocket ds
                    ds.send(packet);

                    // Output if it is the last block
                    if (packet.getLength() < 514) {
                        System.out.println("Last block sent.");
                        return;
                    }

                    try {
                        // Wait for ACK with a new DatagramPacket
                        byte[] ackBuffer = new byte[2];
                        DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);

                        ds.setSoTimeout(5000); // 5-second timeout for ACK
                        ds.receive(packet); // Receive the ACK packet
                        // Get the block number from the ACK packet
                        recievedBlockNumber = packet.getData()[1];

                        // Check if the ACK is valid from the most recently send block
                        if (recievedBlockNumber == blockNumber) {
                            System.out.println("Received ACK for block #" + blockNumber);
                            ackReceived = true; // ACK received, can send the next block
                            blockNumber++; // Increment block number for the next block
                            // recievedBlockNumber++;
                        } else {
                            System.out.println("Invalid ACK received. Resending block #" + blockNumber);
                        }
                        // set the ackReceived to true
                        ackReceived = true;

                    } catch (SocketTimeoutException e) {
                        // Timeout: no ACK received, resend the packet
                        System.out.println("Timeout waiting for ACK, resending block #" + blockNumber);
                        if (ackFailures >= 5) {
                            System.out.println("Too many ACK failures, aborting transfer.");
                            return;
                        }
                        // Increment the number of ack failures
                        ackFailures++;
                    }
                }

                if (lastBlock) {
                    System.out.println("Last block sent.");
                    break; // Exit the loop after sending the last block
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Sends a response to the client y creaking a datagram packet
     * 
     * @param buffer the buffer to send to the client
     */
    public void sendResponse(byte[] buffer) {

        // Send the response to the client
        DatagramPacket response = new DatagramPacket(buffer, buffer.length, req.getAddress(), req.getPort());
        try {
            // Send the response via the DatagramSocket
            socket().send(response);
            System.out.println("Response sent ON PORT " + req.getPort());
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public void run() {
        recieveRequest();
    }

    /**
     * Recieves a request from the client
     */
    public void recieveRequest() {
        // Recieve the request
        // Parse the request
        // Determine the request type
        // Process the request

        // Get the raw data from the request
        byte[] receivedRequest = req.getData(); // Get the raw data
        int len = req.getLength(); // Get the length of the data

        byte[] data = Arrays.copyOfRange(receivedRequest, 1, len);

        byte requestType = receivedRequest[0];

        // Check if the request is a RRQ
        if (requestType == 1) {
            System.out.println("Request is a RRQ");
            String fileName = new String(data);
            System.out.println("Request file: " + fileName);
            // Calls sendfile to send the file to the client
            sendfile(fileName);
        } else if (requestType == 3) {
            System.out.println("You received an ACK before a request");
        }
    }

    /**
     * Creates a socket for the server
     * 
     * @return
     */
    public DatagramSocket socket() {
        if (ds == null) {
            try {
                // Create a new DatagramSocket to recieve the request
                ds = new DatagramSocket(req.getPort());
                System.out.println("Socket created on port " + req.getPort());
            } catch (Exception e) {
                System.err.println("Exception: " + e);
            }
        }
        return ds;
    }

    /**
     * Creates a new TftpServerWorker
     * 
     * @param req the request to be processed
     */
    public TftpServerWorker(DatagramPacket req) {
        this.req = req;
        try {
            this.ds = new DatagramSocket();
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    /**
     * Gets the blocks of a file
     * 
     * @param file the file to get the blocks from
     * @return the blocks of the file
     */
    private List<byte[]> getBlocks(File file) {
        // Create a new ArrayList of byte arrays to store the blocks
        List<byte[]> blocks = new ArrayList<byte[]>();
        // Create a new byte array buffer to store the file data of a single block
        byte[] buffer = new byte[0];

        // Read the file data into the buffer
        try {
            FileInputStream fis = new FileInputStream(file);
            buffer = fis.readAllBytes();
        } catch (Exception e) {
            return null;
        }

        // Calculate the number of complete blocks and the size of the last block
        int numCompleteBlocks = buffer.length / 512;
        int lastBlockSize = buffer.length % 512;

        // Add the complete blocks to the blocks list
        for (int i = 0; i < numCompleteBlocks; i++) {
            byte[] block = new byte[512];
            for (int j = 0; j < 512; j++) {
                block[j] = buffer[i * 512 + j];
            }
            blocks.add(block);
        }

        byte[] block = new byte[lastBlockSize];
        block = Arrays.copyOfRange(buffer, buffer.length - lastBlockSize, buffer.length);
        // Add the last block to the blocks list
        blocks.add(block);
        return blocks;
    }

    public DatagramPacket makePacket(byte packetType, byte blockNumber, byte[] packetData) {

        // Create a new byte array to store the data with the header
        byte[] dataWithHeader = new byte[packetData.length + 2];

        // Set header information of the packet
        dataWithHeader[0] = packetType;
        dataWithHeader[1] = blockNumber;

        // Copy the data to the packet with an offset of 2 not to overwrite the header
        System.arraycopy(packetData, 0, dataWithHeader, 2, packetData.length);

        // System.out.println("Data: " + data);

        return new DatagramPacket(dataWithHeader, dataWithHeader.length, req.getAddress(), req.getPort());
    }

    /**
     * Gets the port of the request
     * 
     * @return the port of the request
     */
    public int getPort() {
        return req.getPort();
    }
}

/**
 * TFTP Server class that listens for requests and sends files to the client
 */
class TftpServer {
    public void start_server() {
        try {
            // Create a new DatagramSocket to listen for requests
            DatagramSocket ds = new DatagramSocket();
            System.out.println("TftpServer on port " + ds.getLocalPort() + " host:" + ds.getLocalAddress());

            // Loop to listen for requests continuously
            for (;;) {
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.receive(p);

                // Prints out the request to the console
                String receivedRequest = new String(buf, 0, p.getLength(), "UTF-8");
                System.out.println("Request:" + receivedRequest);

                // Create a new TftpServerWorker to process the request
                TftpServerWorker worker = new TftpServerWorker(p);
                System.out.println("Worker created on port " + worker.getPort());
                worker.start();
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

        return;
    }

    /**
     * Main method to start the server
     * 
     * @param args
     */
    public static void main(String args[]) {
        TftpServer d = new TftpServer();
        d.start_server();
    }

}