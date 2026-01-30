package tp1.chatroom;

public interface ChatRoom {
    boolean subscribe(String username, int clientPort);
    boolean unsubscribe(String username);
    boolean postMessage(String username, String message);
}
