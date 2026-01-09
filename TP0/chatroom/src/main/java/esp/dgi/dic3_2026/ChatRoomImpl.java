package esp.dgi.dic3_2026;

import esp.dgi.dic3_2026.exceptions.SubscribeAndUnsubscribeException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class ChatRoomImpl extends UnicastRemoteObject implements ChatRoom {

    private final HashMap<String, ChatRoomUser> subscribers = new HashMap<>();
    public ChatRoomImpl() throws RemoteException {
    }

    @Override
    public void subscribe(ChatRoomUser client, String pseudonym) throws RemoteException {
        if (isSubscribed(pseudonym))
            throw new SubscribeAndUnsubscribeException(pseudonym + " has subscribed.");
        subscribers.put(pseudonym, client);
        System.out.println(pseudonym + " has subscribed.");
    }

    @Override
    public void unsubscribe(String pseudonym) throws RemoteException {
        if (!isSubscribed(pseudonym))
            throw new SubscribeAndUnsubscribeException(pseudonym + " has not subscribed.");
        subscribers.remove(pseudonym);
        System.out.println(pseudonym + " has unsubscribed.");
    }

    @Override
    public void postMessage(String pseudonym, String message) throws RemoteException {
        System.out.println("Message from " + pseudonym + ": " + message);
        for (String clientPseudonym : subscribers.keySet()) {
            if (!clientPseudonym.equals(pseudonym)) {
                ChatRoomUser client = getClient(clientPseudonym);
                client.displayMessage(pseudonym, message);
            }
        }
    }

    private boolean isSubscribed (String pseudonym) {
        return subscribers.containsKey(pseudonym) && subscribers.get(pseudonym) != null;
    }

    private ChatRoomUser getClient (String pseudonym) throws RemoteException {
        if (subscribers.get(pseudonym) == null)
            throw new SubscribeAndUnsubscribeException(pseudonym + " has not subscribed.");
        return subscribers.get(pseudonym);
    }
}
