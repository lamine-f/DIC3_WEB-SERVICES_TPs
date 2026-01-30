package tp1.chatroom_polling;

import java.util.*;

public class ChatRoomServer implements ChatRoom {

    private final Set<String> subscribers = new HashSet<>();

    private final Map<String, List<String>> pendingMessages = new HashMap<>();

    public synchronized boolean subscribe(String username) {
        if (subscribers.contains(username)) {
            System.out.println("Subscription failed: " + username + " is already subscribed.");
            return false;
        }
        subscribers.add(username);
        pendingMessages.put(username, new ArrayList<>());
        System.out.println(username + " has subscribed.");
        return true;
    }

    public synchronized boolean unsubscribe(String username) {
        if (!subscribers.contains(username)) {
            System.out.println("Unsubscription failed: " + username + " is not subscribed.");
            return false;
        }
        subscribers.remove(username);
        pendingMessages.remove(username);
        System.out.println(username + " has unsubscribed.");
        return true;
    }

    public synchronized boolean postMessage(String username, String message) {
        if (!subscribers.contains(username)) {
            System.out.println("Post failed: " + username + " is not subscribed.");
            return false;
        }

        System.out.println("Message from " + username + ": " + message);

        String formattedMessage = username + ": " + message;

        for (String subscriber : subscribers) {
            if (!subscriber.equals(username)) {
                pendingMessages.get(subscriber).add(formattedMessage);
            }
        }
        return true;
    }

    public synchronized Vector<String> getMessages(String username) {
        Vector<String> messages = new Vector<>();

        if (!subscribers.contains(username)) return messages;

        List<String> pending = pendingMessages.get(username);
        if (pending != null && !pending.isEmpty()) {
            messages.addAll(pending);
            pending.clear();
        }

        return messages;
    }
}
