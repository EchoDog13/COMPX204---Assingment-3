import java.net.*;
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

            DatagramSocket packet = new DatagramSocket();

            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            int character;
            byte buffer[] = new byte[512];

            while ((character = fis.read(buffer)) != -1) {
                // Printing out for testing purposes
                System.out.print((char) character);

                // Send the data to the receiver
                packet.send(new DatagramPacket(buffer, character, req.getAddress(), req.getPort()));

            }
            return;

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void run() {
        /*
         * parse the request packet, ensuring that it is a RRQ
         * and then call sendfile
         */

        // Print the name of the file requested
        System.out.println(getName());

        sendfile(getName());
        return;
    }

    public TftpServerWorker(DatagramPacket req) {
        this.req = req;
    }
}

class TftpServer {
    public void start_server() {
        try {
            DatagramSocket ds = new DatagramSocket();
            InetAddress address = ds.getInetAddress();
            System.out.println("TftpServer on port " + ds.getLocalPort() + " host:" + ds.getLocalAddress());

            for (;;) {
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.receive(p);
                System.out.println("Received request from " + p.getAddress() + " on port " + p.getPort());

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