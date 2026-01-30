package tp1.chatroom_polling;

import java.util.Vector;

public interface ChatRoom {
    boolean subscribe(String username);
    boolean unsubscribe(String username);
    boolean postMessage(String username, String message);
    Vector<String> getMessages(String username);
}
