package tp1.chatroom_polling;

import org.apache.xmlrpc.XmlRpcClient;

import static tp1.chatroom_polling.ServerApp.SERVER_PORT;

public class ClientApp {

    public static void main(String[] args) {
        try {
            XmlRpcClient serverClient = new XmlRpcClient("http://localhost:"+SERVER_PORT+"/RPC2");
            System.out.println("Starting ChatClient (Polling version)...");
            new ChatClient(serverClient);
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
