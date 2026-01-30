import java.net.ServerSocket;

public class ClientApp {

    private static final int BASE_PORT = 8001;
    private static int portCounter = 0;

    public static void main(String[] args) {
        int clientPort = findAvailablePort();
        System.out.println("Starting ChatClient on port " + clientPort);
        new ChatClient(clientPort);
    }

    private static int findAvailablePort() {
        int port = BASE_PORT + portCounter;
        portCounter++;
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
            return port;
        } catch (Exception e) {
            return findAvailablePort();
        }
    }
}
