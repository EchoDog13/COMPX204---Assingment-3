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
    private static boolean sendNextPacket = true;

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

        try {

            File file = new File(filename);
            if (file.exists() == false) {
                System.out.println("File does not exist");
                // Send error message
                return;

            }
            int numBlocks = (int) Math.ceil(file.length() / 512.0);
            FileInputStream fis = new FileInputStream(file);
            int character;
            int charCount = 0;

            // byte sendingBuffer[] = new byte[514];
            int blockNumber = 0;

            System.out.println("Sending port:" + req.getPort() + " Address:" + req.getAddress());

            // Read the file
            for (int i = 0; i < numBlocks; i++) {

                // Printing out for testing purposes
                // fis.readNBytes(sendingBuffer, 2, 512 - 1);

                int bytesRead = 0;
                int maxBytes = 512;
                byte[] buffer = new byte[maxBytes];

                while (bytesRead < maxBytes) {
                    int b = fis.read(); // read a byte
                    if (b == -1 || b == 0) { // end of file or null byte
                        break;
                    }
                    buffer[bytesRead] = (byte) b;
                    bytesRead++;
                }

                // Optionally copy the valid bytes to a smaller array if needed
                byte[] sendingBuffer = new byte[bytesRead];
                System.arraycopy(buffer, 0, sendingBuffer, 0, bytesRead);

                byte[] shiftedArray = shiftArray(sendingBuffer, 2);

                shiftedArray[0] = "2".getBytes()[0];
                shiftedArray[1] = String.valueOf(blockNumber).getBytes()[0];

                blockNumber++;
                sendResponse(shiftedArray);
                sendNextPacket = false;
            }

            return;

        } catch (Exception e) {
            // TODO: handle exception
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
        /*
         * parse the request packet, ensuring that it is a RRQ
         * and then call sendfile
         */

        // Read Request to plain text
        // Determine Request Type
        // Process according
        // TftpServerWorker worker;
        byte[] data = req.getData(); // Get the raw data
        int len = req.getLength(); // Get the length of the data

        String receivedRequest = null;
        try {
            receivedRequest = new String(data, 0, len, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Check if the request is a RRQ
        if (receivedRequest.startsWith("1")) {
            System.out.println("Request is a RRQ");

            // Get file name
            String[] request = receivedRequest.split(" ");
            String fileName = request[1];

            sendfile(fileName);
        } else if (receivedRequest.startsWith("3")) {
            System.out.println("ACK Received. Block #" + receivedRequest.charAt(1));
            sendNextPacket = true;

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