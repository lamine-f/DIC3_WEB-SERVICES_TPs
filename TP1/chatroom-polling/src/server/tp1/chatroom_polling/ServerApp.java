package tp1.chatroom_polling;

import org.apache.xmlrpc.WebServer;

public class ServerApp {

    public final static int SERVER_PORT = 8888 + 1;

    public static void main(String[] args) {
        try {
            System.out.println("Starting ChatRoom XML-RPC Server (Polling version)...");
            WebServer server = new WebServer(SERVER_PORT);
            server.addHandler("chatroom", new ChatRoomServer());
            server.start();
            System.out.println("ChatRoom Server started on port " + (SERVER_PORT));
        } catch (Exception e) {
            System.err.println("ServerApp: " + e);
            e.printStackTrace();
        }
    }
}
