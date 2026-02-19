package tp1.chatroom;

import org.apache.xmlrpc.XmlRpcClient;

import java.net.ServerSocket;

public class ClientApp {

    private static final int BASE_PORT = 8001;
    private static int portCounter = 0;

    public static void main(String[] args) {
        try {
            XmlRpcClient serverClient = new XmlRpcClient("http://localhost:9999/RPC2");
            System.out.println("Connected to main server");
            int clientPort = findAvailablePort();
            System.out.println("Starting ChatClient on port " + clientPort);
            new ChatClient(serverClient, clientPort);
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e);
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static int findAvailablePort() {
        int port = BASE_PORT + portCounter;
        portCounter++;
        try {
            new ServerSocket(port).close();
            return port;
        } catch (Exception e) {
            return findAvailablePort();
        }
    }
}
