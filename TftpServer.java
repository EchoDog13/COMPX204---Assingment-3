import java.net.*;
import java.nio.Buffer;
import java.io.*;
import java.util.*;

class TftpServerWorker extends Thread {
    private DatagramPacket req;
    private static final byte RRQ = 1; // Read Request
    private static final byte DATA = 2;
    private static final byte ACK = 3;
    private static final byte ERROR = 4;
    private byte recievedBlockNumber = 0;
    // DatagramPacket ackPacket;

    private DatagramSocket ds = null;

    /**
     * Sends a file to the receiver.
     * 
     * @param filename the name of the file to be sent
     */
    private void sendfile(String filename) {

        /*
         * open the file using a FileInputStream and send it, one block at
         * a time, to the receiver.
         */

        // Open File using openFileInputStream
        // Send it to the receiver in chucks of 512 bytes

        // try {

        // File file = new File(filename);
        // if (file.exists() == false) {
        // System.out.println("File does not exist");
        // // Send error message
        // return;

        // }
        // int numBlocks = (int) Math.ceil(file.length() / 512.0);
        // FileInputStream fis = new FileInputStream(file);
        // int character;
        // int charCount = 0;

        // // byte sendingBuffer[] = new byte[514];
        // byte blockNumber = 0;

        // System.out.println("Sending port:" + req.getPort() + " Address:" +
        // req.getAddress());

        // List<byte[]> blocks = getBlocks(file);

        // Read the file
        // for (int i = 0; i < blocks.size(); i++) {

        // // Printing out for testing purposes

        // // fis.readNBytes(sendingBuffer, 2, 512 - 1);
        // // Sending the data
        // // while (recievedBlockNumber != blockNumber) {

        // byte[] currentBlock = blocks.get(i);
        // DatagramPacket packet = makePacket(DATA, (byte) blockNumber, currentBlock);

        // ds.send(packet);
        // // recieveRequest();

        // // Wait for ACK

        // // ds.setSoTimeout(5000);
        // // ds.send(packet);

        // /// recieveRequest();
        // // if (recievedBlockNumber == blockNumber) {
        // // blockNumber++;

        // // break;
        // // }

        // // }

        // }

        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("File does not exist");
                // Send error message
                return;
            }

            List<byte[]> blocks = getBlocks(file);
            byte blockNumber = 0; // TFTP block number starts at 1
            boolean lastBlock = false;

            for (int i = 0; i < blocks.size(); i++) {
                byte[] currentBlock = blocks.get(i);

                // Check if this is the last block
                lastBlock = currentBlock.length < 512;

                boolean ackReceived = false;
                while (!ackReceived) {
                    // Create the packet to send the current block
                    DatagramPacket packet = makePacket(DATA, blockNumber, currentBlock);
                    ds.send(packet);

                    try {
                        // Wait for ACK with a new DatagramPacket
                        byte[] ackBuffer = new byte[2];
                        DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);

                        ds.setSoTimeout(5000); // 5-second timeout for ACK
                        ds.receive(packet);

                        byte[] ackData = ackPacket.getData();
                        // if (ackData[0] == ACK && ackData[1] == blockNumber) {
                        // System.out.println("Received ACK for block #" + blockNumber);
                        // ackReceived = true; // ACK received, can send the next block
                        // blockNumber++; // Increment block number for the next block
                        // }
                        if (recievedBlockNumber == blockNumber) {
                            System.out.println("Received ACK for block #" + blockNumber);
                            ackReceived = true; // ACK received, can send the next block
                            blockNumber++; // Increment block number for the next block
                            recievedBlockNumber++;

                        }

                        else {
                            System.out.println("Invalid ACK received. Resending block #" + blockNumber);
                        }
                        ackReceived = true;

                    } catch (SocketTimeoutException e) {
                        // Timeout: no ACK received, resend the packet
                        System.out.println("Timeout waiting for ACK, resending block #" + blockNumber);
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

    public static byte[] shiftArray(byte[] array, int positions) {
        int length = array.length;
        byte[] result = new byte[length];

        // Handle cases where positions >= length
        positions = positions % length;
        if (positions < 0) {
            positions += length;
        }

        // Perform the shift
        for (int i = 0; i < length; i++) {
            result[(i + positions) % length] = array[i];
        }

        return result;
    }

    public void sendResponse(byte[] buffer) {
        // Send the response to the client

        DatagramPacket response = new DatagramPacket(buffer, buffer.length, req.getAddress(), req.getPort());
        try {
            socket().send(response);
            System.out.println("Response sent ON PORT " + req.getPort());
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

    }

    public void run() {
        recieveRequest();
        /*
         * parse the request packet, ensuring that it is a RRQ
         * and then call sendfile
         */

        // Read Request to plain text
        // Determine Request Type
        // Process according
        // TftpServerWorker worker;

    }

    public void recieveRequest() {
        // Recieve the request
        // Parse the request
        // Determine the request type
        // Process the request
        // // System.out.println("Recieving Request");

        byte[] data = req.getData(); // Get the raw data
        int len = req.getLength(); // Get the length of the data

        String receivedRequest = null;
        try {
            receivedRequest = new String(data, 0, len, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String requestType = Character.toString((char) data[0]);

        // Check if the request is a RRQ
        if (requestType.equals("1")) {
            System.out.println("Request is a RRQ");

            // Get file nameç
            String[] request = receivedRequest.split(" ");
            String fileName = request[1];

            sendfile(fileName);
        } else if (requestType.equals("3")) {
            System.out.println("ACK Received. Block #" + receivedRequest.charAt(1));
            // recievedBlockNumber = (byte) receivedRequest.charAt(1);
            // ackPacket = req;

            return;
        }
        return;

    }

    public DatagramSocket socket() {

        if (ds == null) {
            try {
                ds = new DatagramSocket(req.getPort());
                System.out.println("Socket created on port " + req.getPort());
            } catch (Exception e) {
                System.err.println("Exception: " + e);
            }
        }
        return ds;
    }

    public class TftpServerManager {
        public static TftpServer server;

        static {
            server = new TftpServer();
        }
    }

    public TftpServerWorker(DatagramPacket req, DatagramSocket ds) {
        this.req = req;
        this.ds = ds;
    }

    private List<byte[]> getBlocks(File file) {
        List<byte[]> blocks = new ArrayList<byte[]>();
        byte[] buffer = new byte[0];

        try {
            FileInputStream fis = new FileInputStream(file);
            buffer = fis.readAllBytes();
        } catch (Exception e) {
            return null;
        }

        int numCompleteBlocks = buffer.length / 512;
        int lastBlockSize = buffer.length % 512;

        for (int i = 0; i < numCompleteBlocks; i++) {
            byte[] block = new byte[512];
            for (int j = 0; j < 512; j++) {
                block[j] = buffer[i * 512 + j];
            }
            blocks.add(block);
        }

        byte[] block = new byte[lastBlockSize];
        block = Arrays.copyOfRange(buffer, buffer.length - lastBlockSize, buffer.length);
        blocks.add(block);
        return blocks;
    }

    public DatagramPacket makePacket(byte packetType, byte blockNumber, byte[] packetData) {

        byte[] dataWithHeader = new byte[packetData.length + 2];

        dataWithHeader[0] = packetType;
        dataWithHeader[1] = blockNumber;

        System.arraycopy(packetData, 0, dataWithHeader, 2, packetData.length);

        // System.out.println("Data: " + data);

        return new DatagramPacket(dataWithHeader, dataWithHeader.length, req.getAddress(), req.getPort());
    }
}

class TftpServer {
    public void start_server() {
        try {
            DatagramSocket ds = new DatagramSocket(20202);
            System.out.println("TftpServer on port " + ds.getLocalPort() + " host:" + ds.getLocalAddress());

            for (;;) {
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.receive(p);
                // System.out.println("Received request from " + p.getAddress() + " on port " +
                // p.getPort());
                // Prints out the request to the console
                String receivedRequest = new String(buf, 0, p.getLength(), "UTF-8");
                System.out.println("Request:" + receivedRequest);

                TftpServerWorker worker = new TftpServerWorker(p, ds);
                worker.start();
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }

        return;
    }

    public static void main(String args[]) {
        TftpServer d = new TftpServer();
        d.start_server();
    }

}