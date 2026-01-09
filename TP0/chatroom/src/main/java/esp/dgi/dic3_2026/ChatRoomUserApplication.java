package esp.dgi.dic3_2026;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ChatRoomUserApplication {

    static int PORT = 1099;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(PORT);
            ChatRoom stub = (ChatRoom) registry.lookup("ChatRoomServer");
            new ChatRoomMessagingUI(stub);
            System.out.println("Client is running...");
        }catch (Exception ex) {
            System.err.println("Error in client: " + ex.getMessage());
        }
    }
}
