package esp.dgi.dic3_2026;

import esp.dgi.dic3_2026.exceptions.SubscriptionException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class ChatRoomServer extends UnicastRemoteObject implements ChatRoom {

    private final HashMap<String, MessageListener> subscribers = new HashMap<>();

    public ChatRoomServer() throws RemoteException {
    }

    @Override
    public void subscribe(MessageListener client, String username) throws RemoteException {
        if (isSubscribed(username))
            throw new SubscriptionException(username + " has subscribed.");
        subscribers.put(username, client);
        System.out.println(username + " has subscribed.");
    }

    @Override
    public void unsubscribe(String username) throws RemoteException {
        if (!isSubscribed(username))
            throw new SubscriptionException(username + " has not subscribed.");
        subscribers.remove(username);
        System.out.println(username + " has unsubscribed.");
    }

    @Override
    public void postMessage(String username, String message) throws RemoteException {
        System.out.println("Message from " + username + ": " + message);
        for (String clientUsername : subscribers.keySet()) {
            if (!clientUsername.equals(username)) {
                MessageListener client = getClient(clientUsername);
                client.displayMessage(username, message);
            }
        }
    }

    private boolean isSubscribed(String username) {
        return subscribers.containsKey(username) && subscribers.get(username) != null;
    }

    private MessageListener getClient(String username) throws RemoteException {
        if (subscribers.get(username) == null)
            throw new SubscriptionException(username + " has not subscribed.");
        return subscribers.get(username);
    }
}
