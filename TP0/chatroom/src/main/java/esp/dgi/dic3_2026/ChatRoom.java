package esp.dgi.dic3_2026;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatRoom extends Remote {
    void subscribe(ChatRoomUser client, String pseudonym) throws RemoteException;
    void unsubscribe(String pseudonym) throws RemoteException;
    void postMessage(String pseudonym, String message) throws RemoteException;
}
