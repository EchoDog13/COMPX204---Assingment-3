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

    private void sendfile(String filename) {

        /*
         * open the file using a FileInputStream and send it, one block at
         * a time, to the receiver.
         */

        // Open File using openFileInputStream
        // Send it to the receiver in chucks of 512 bytes

        try {

            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            int character;

            byte[] data = new byte[512];
            int blockNumber = 0;
            System.out.println("Sending port:" + req.getPort() + " Address:" + req.getAddress());

            while ((character = fis.read(data)) != -1) {
                // Printing out for testing purposes

                byte sendingBuffer[] = new byte[514];
                ; // 2 bytes for opcode, 2 for block number, rest for data

                // Set the opcode (DATA = 03)
                sendingBuffer[0] = 0;
                sendingBuffer[1] = (byte) blockNumber;
                blockNumber++;

                // Send the data to the receiver - takes byte array
                sendResponse(sendingBuffer);

            }
            return;

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void sendResponse(byte[] buffer) {
        // Send the response to the client
        DatagramPacket response = new DatagramPacket(buffer, buffer.length, req.getAddress(), req.getPort());
        // packet.send(response);

    }

    public void run() {
        /*
         * parse the request packet, ensuring that it is a RRQ
         * and then call sendfile
         */

        // Print the name of the file requested
        // System.out.println(getName());

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
        } else {
            System.out.println("Request is not a RRQ");
            return;
        }

        return;

    }

    public TftpServerWorker(DatagramPacket req) {
        this.req = req;

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
                System.out.println("Received request from " + p.getAddress() + " on port " + p.getPort());
                // Prints out the request to the console
                String receivedRequest = new String(buf, 0, p.getLength(), "UTF-8");
                System.out.println("Request:" + receivedRequest);

                TftpServerWorker worker = new TftpServerWorker(p);
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