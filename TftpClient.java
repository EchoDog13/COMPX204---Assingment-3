import java.net.*;

public class TftpClient {

    /**
     * 
     * @param args
     */
    public static void main(String args[]) {
        String hostName = null;
        Integer port = 69;
        String filePath = null;
        ;

        // Read in argument
        if (args.length >= 3) {
            hostName = args[0];
            port = Integer.parseInt(args[1]);
            filePath = args[2];
        }

        // Send RRQ Packet to Server to request file
        try {
            DatagramSocket ds = new DatagramSocket();
            InetAddress addr = InetAddress.getByName(hostName);

            byte[] buf = new byte[1472];
            buf = ("RRQ " + filePath).getBytes();
            DatagramPacket p = new DatagramPacket(buf, buf.length, addr, port);

            ds.send(p);
            // ds.receive(p);
            System.out.println(new String(p.getData(), 0, p.getLength()));

            ds.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }
}
