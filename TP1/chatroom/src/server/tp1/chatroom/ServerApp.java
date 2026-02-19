package tp1.chatroom;

import org.apache.xmlrpc.WebServer;

public class ServerApp {

    public static void main(String[] args) {
        try {
            System.out.println("Starting ChatRoom XML-RPC Server...");
            WebServer server = new WebServer(9999);
            server.addHandler("chatroom", new ChatRoomServer());
            server.start();
            System.out.println("ChatRoom Server started on port 9999");
            System.out.println("Accepting requests. (Halt program to stop.)");
        } catch (Exception e) {
            System.err.println("ServerApp: " + e);
            e.printStackTrace();
        }
    }
}
