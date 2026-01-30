package tp1.chatroom_polling;

import org.apache.xmlrpc.WebServer;

public class ServerApp {

    public static void main(String[] args) {
        try {
            System.out.println("Starting ChatRoom XML-RPC Server (Polling version)...");
            WebServer server = new WebServer(80);
            server.addHandler("chatroom", new ChatRoomServer());
            server.start();
            System.out.println("ChatRoom Server started on port 80");
            System.out.println("Accepting requests. (Halt program to stop.)");
        } catch (Exception e) {
            System.err.println("ServerApp: " + e);
            e.printStackTrace();
        }
    }
}
