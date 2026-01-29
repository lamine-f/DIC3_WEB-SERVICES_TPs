package esp.dgi.dic3_2026;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerApp {
    static int PORT = 1099;

    public static void main(String[] args) {
        try {
            ChatRoom chatRoom = new ChatRoomServer();
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.bind("ChatRoomServer", chatRoom);
            System.out.println("ChatRoomServer is running...");
        } catch (Exception ex) {
            System.err.println("Error in ChatRoomServer: " + ex.getMessage());
        }
    }
}
